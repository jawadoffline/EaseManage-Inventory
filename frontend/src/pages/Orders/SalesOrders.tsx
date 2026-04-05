import { useState, useMemo } from 'react';
import {
  Table,
  Button,
  Input,
  Modal,
  Form,
  Select,
  Tag,
  Breadcrumb,
  Typography,
  Space,
  InputNumber,
  Dropdown,
  message,
  Popconfirm,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  DownOutlined,
  MinusCircleOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import { exportToCsv } from '../../utils/csvExport';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { salesOrdersApi } from '../../api/orders';
import type { SalesOrder, SalesOrderRequest } from '../../api/orders';
import { warehousesApi } from '../../api/warehouses';
import { productsApi } from '../../api/products';
import dayjs from 'dayjs';

const { Title } = Typography;
const { Search } = Input;

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'orange',
  PROCESSING: 'blue',
  SHIPPED: 'cyan',
  DELIVERED: 'green',
  CANCELLED: 'red',
};

const SalesOrders: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);

  // ---- Queries ----

  const { data, isLoading } = useQuery({
    queryKey: ['sales-orders', page, statusFilter, search],
    queryFn: () =>
      salesOrdersApi.getAll({
        page: page - 1,
        size: pageSize,
        status: statusFilter,
        search: search || undefined,
      }),
  });

  const { data: warehouses } = useQuery({
    queryKey: ['warehouses-active'],
    queryFn: () => warehousesApi.getAllActive(),
  });

  const { data: productsData } = useQuery({
    queryKey: ['products-all'],
    queryFn: () => productsApi.getAll({ size: 100 }),
  });

  const products = productsData?.content ?? [];

  // ---- Mutations ----

  const createMutation = useMutation({
    mutationFn: (data: SalesOrderRequest) => salesOrdersApi.create(data),
    onSuccess: () => {
      message.success('Sales order created successfully');
      queryClient.invalidateQueries({ queryKey: ['sales-orders'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to create sales order');
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      salesOrdersApi.updateStatus(id, status),
    onSuccess: () => {
      message.success('Order status updated successfully');
      queryClient.invalidateQueries({ queryKey: ['sales-orders'] });
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to update order status');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => salesOrdersApi.delete(id),
    onSuccess: () => {
      message.success('Sales order deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['sales-orders'] });
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to delete sales order');
    },
  });

  // ---- Modal helpers ----

  const openCreateModal = () => {
    form.resetFields();
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    form.resetFields();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      createMutation.mutate({
        customerName: values.customerName,
        warehouseId: values.warehouseId,
        shippingAddress: values.shippingAddress,
        items: values.items,
      });
    } catch {
      // validation failed
    }
  };

  // ---- Computed total for modal ----

  const FormTotal: React.FC = () => {
    const items = Form.useWatch('items', form);
    const total = useMemo(() => {
      if (!items || !Array.isArray(items)) return 0;
      return items.reduce((sum: number, item: { quantity?: number; unitPrice?: number }) => {
        const qty = item?.quantity ?? 0;
        const price = item?.unitPrice ?? 0;
        return sum + qty * price;
      }, 0);
    }, [items]);

    return (
      <div style={{ textAlign: 'right', fontWeight: 600, fontSize: 16, marginTop: 8 }}>
        Total: ${total.toFixed(2)}
      </div>
    );
  };

  // ---- Action menu builder ----

  const getActionItems = (record: SalesOrder) => {
    const items: { key: string; label: string; danger?: boolean; onClick: () => void }[] = [];

    if (record.status === 'PENDING') {
      items.push({
        key: 'process',
        label: 'Process',
        onClick: () => updateStatusMutation.mutate({ id: record.id, status: 'PROCESSING' }),
      });
    }
    if (record.status === 'PROCESSING') {
      items.push({
        key: 'ship',
        label: 'Ship',
        onClick: () => updateStatusMutation.mutate({ id: record.id, status: 'SHIPPED' }),
      });
    }
    if (record.status === 'SHIPPED') {
      items.push({
        key: 'deliver',
        label: 'Mark Delivered',
        onClick: () => updateStatusMutation.mutate({ id: record.id, status: 'DELIVERED' }),
      });
    }
    if (record.status !== 'CANCELLED' && record.status !== 'DELIVERED') {
      items.push({
        key: 'cancel',
        label: 'Cancel',
        danger: true,
        onClick: () => updateStatusMutation.mutate({ id: record.id, status: 'CANCELLED' }),
      });
    }

    items.push({
      key: 'pdf',
      label: 'Download PDF',
      onClick: () => salesOrdersApi.downloadSalesOrderPdf(record.id),
    });

    return items;
  };

  const handleExport = () => {
    const rows = (data?.content ?? []).map((item: SalesOrder) => [
      item.orderNumber,
      item.customerName,
      item.warehouseName,
      item.status,
      item.totalAmount,
      item.createdAt ? dayjs(item.createdAt).format('YYYY-MM-DD HH:mm') : '',
    ]);
    exportToCsv('sales_orders.csv', ['Order #', 'Customer', 'Warehouse', 'Status', 'Total Amount', 'Created At'], rows);
  };

  // ---- Table columns ----

  const columns = [
    {
      title: 'Order #',
      dataIndex: 'orderNumber',
      key: 'orderNumber',
      render: (val: string) => (
        <Tag color="green" style={{ fontFamily: 'monospace' }}>
          {val}
        </Tag>
      ),
    },
    {
      title: 'Customer Name',
      dataIndex: 'customerName',
      key: 'customerName',
    },
    {
      title: 'Warehouse',
      dataIndex: 'warehouseName',
      key: 'warehouseName',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={STATUS_COLORS[status] ?? 'default'}>{status}</Tag>
      ),
    },
    {
      title: 'Total Amount',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (val: number) => `$${val.toFixed(2)}`,
    },
    {
      title: 'Created By',
      dataIndex: 'createdByName',
      key: 'createdByName',
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: SalesOrder) => {
        const actionItems = getActionItems(record);
        return (
          <Space>
            {actionItems.length > 0 && (
              <Dropdown menu={{ items: actionItems }} trigger={['click']}>
                <Button type="link">
                  Actions <DownOutlined />
                </Button>
              </Dropdown>
            )}
            {record.status === 'PENDING' && (
              <Popconfirm
                title="Delete sales order"
                description="Are you sure you want to delete this sales order?"
                onConfirm={() => deleteMutation.mutate(record.id)}
                okText="Yes"
                cancelText="No"
              >
                <Button type="link" danger icon={<DeleteOutlined />}>
                  Delete
                </Button>
              </Popconfirm>
            )}
          </Space>
        );
      },
    },
  ];

  // ---- Render ----

  return (
    <div>
      <Breadcrumb
        items={[{ title: 'Home' }, { title: 'Sales Orders' }]}
        style={{ marginBottom: 16 }}
      />

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 24,
        }}
      >
        <Title level={3} style={{ margin: 0 }}>
          Sales Orders
        </Title>
        <Space>
          <Search
            placeholder="Search sales orders..."
            allowClear
            onSearch={(value) => {
              setSearch(value);
              setPage(1);
            }}
            style={{ width: 250 }}
          />
          <Select
            placeholder="Filter by status"
            allowClear
            style={{ width: 160 }}
            value={statusFilter}
            onChange={(value) => {
              setStatusFilter(value);
              setPage(1);
            }}
            options={[
              { value: 'PENDING', label: 'Pending' },
              { value: 'PROCESSING', label: 'Processing' },
              { value: 'SHIPPED', label: 'Shipped' },
              { value: 'DELIVERED', label: 'Delivered' },
              { value: 'CANCELLED', label: 'Cancelled' },
            ]}
          />
          <Button icon={<DownloadOutlined />} onClick={handleExport}>Export</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            New Sales Order
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={data?.content ?? []}
        rowKey="id"
        loading={isLoading}
        pagination={{
          current: page,
          pageSize,
          total: data?.totalElements ?? 0,
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} sales orders`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title="New Sales Order"
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={closeModal}
        confirmLoading={createMutation.isPending}
        width={720}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="customerName"
            label="Customer Name"
            rules={[{ required: true, message: 'Customer name is required' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="warehouseId"
            label="Warehouse"
            rules={[{ required: true, message: 'Warehouse is required' }]}
          >
            <Select
              placeholder="Select warehouse"
              options={(warehouses ?? []).map((w) => ({
                value: w.id,
                label: w.name,
              }))}
            />
          </Form.Item>

          <Form.Item name="shippingAddress" label="Shipping Address">
            <Input.TextArea rows={2} />
          </Form.Item>

          <Form.List
            name="items"
            rules={[
              {
                validator: async (_, items) => {
                  if (!items || items.length === 0) {
                    return Promise.reject(new Error('At least one item is required'));
                  }
                },
              },
            ]}
          >
            {(fields, { add, remove }, { errors }) => (
              <>
                {fields.map(({ key, name, ...restField }) => (
                  <Space
                    key={key}
                    style={{ display: 'flex', marginBottom: 8 }}
                    align="baseline"
                  >
                    <Form.Item
                      {...restField}
                      name={[name, 'productId']}
                      rules={[{ required: true, message: 'Select a product' }]}
                    >
                      <Select
                        placeholder="Product"
                        style={{ width: 240 }}
                        showSearch
                        optionFilterProp="label"
                        options={products.map((p) => ({
                          value: p.id,
                          label: `${p.name} (${p.sku})`,
                        }))}
                      />
                    </Form.Item>
                    <Form.Item
                      {...restField}
                      name={[name, 'quantity']}
                      rules={[{ required: true, message: 'Qty required' }]}
                    >
                      <InputNumber placeholder="Qty" min={1} style={{ width: 100 }} />
                    </Form.Item>
                    <Form.Item
                      {...restField}
                      name={[name, 'unitPrice']}
                      rules={[{ required: true, message: 'Price required' }]}
                    >
                      <InputNumber
                        placeholder="Unit Price"
                        min={0}
                        step={0.01}
                        style={{ width: 130 }}
                        prefix="$"
                      />
                    </Form.Item>
                    <MinusCircleOutlined onClick={() => remove(name)} />
                  </Space>
                ))}
                <Form.Item>
                  <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                    Add Item
                  </Button>
                  <Form.ErrorList errors={errors} />
                </Form.Item>
              </>
            )}
          </Form.List>

          <FormTotal />
        </Form>
      </Modal>
    </div>
  );
};

export default SalesOrders;
