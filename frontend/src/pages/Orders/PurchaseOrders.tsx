import { useState } from 'react';
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
  message,
  Popconfirm,
  Dropdown,
  InputNumber,
  DatePicker,
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
import { purchaseOrdersApi } from '../../api/orders';
import type { PurchaseOrder, PurchaseOrderRequest } from '../../api/orders';
import { suppliersApi } from '../../api/suppliers';
import { warehousesApi } from '../../api/warehouses';
import { productsApi } from '../../api/products';
import dayjs from 'dayjs';

const { Title } = Typography;
const { Search } = Input;
const { TextArea } = Input;

const STATUS_COLORS: Record<string, string> = {
  DRAFT: 'default',
  PENDING: 'orange',
  APPROVED: 'blue',
  RECEIVED: 'green',
  CANCELLED: 'red',
};

const PurchaseOrders: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);

  // ---- Queries ----

  const { data, isLoading } = useQuery({
    queryKey: ['purchase-orders', page, statusFilter, search],
    queryFn: () =>
      purchaseOrdersApi.getAll({
        page: page - 1,
        size: pageSize,
        status: statusFilter,
        search: search || undefined,
      }),
  });

  const { data: suppliers } = useQuery({
    queryKey: ['suppliers-active'],
    queryFn: () => suppliersApi.getAllActive(),
  });

  const { data: warehouses } = useQuery({
    queryKey: ['warehouses-active'],
    queryFn: () => warehousesApi.getAllActive(),
  });

  const { data: productsData } = useQuery({
    queryKey: ['products-all'],
    queryFn: () => productsApi.getAll({ size: 100 }),
  });

  // ---- Mutations ----

  const createMutation = useMutation({
    mutationFn: (data: PurchaseOrderRequest) => purchaseOrdersApi.create(data),
    onSuccess: () => {
      message.success('Purchase order created successfully');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to create purchase order');
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      purchaseOrdersApi.updateStatus(id, status),
    onSuccess: () => {
      message.success('Status updated successfully');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to update status');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => purchaseOrdersApi.delete(id),
    onSuccess: () => {
      message.success('Purchase order deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to delete purchase order');
    },
  });

  // ---- Handlers ----

  const handleExport = () => {
    const rows = (data?.content ?? []).map((item: PurchaseOrder) => [
      item.orderNumber,
      item.supplierName,
      item.warehouseName,
      item.status,
      item.totalAmount,
      item.expectedDelivery ? dayjs(item.expectedDelivery).format('YYYY-MM-DD') : '',
      item.createdAt ? dayjs(item.createdAt).format('YYYY-MM-DD HH:mm') : '',
    ]);
    exportToCsv('purchase_orders.csv', ['Order #', 'Supplier', 'Warehouse', 'Status', 'Total Amount', 'Expected Delivery', 'Created At'], rows);
  };

  const closeModal = () => {
    setModalOpen(false);
    form.resetFields();
  };

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      const payload: PurchaseOrderRequest = {
        supplierId: values.supplierId,
        warehouseId: values.warehouseId,
        expectedDelivery: values.expectedDelivery
          ? dayjs(values.expectedDelivery).format('YYYY-MM-DD')
          : undefined,
        notes: values.notes || undefined,
        items: (values.items || []).map(
          (item: { productId: number; quantity: number; unitPrice: number }) => ({
            productId: item.productId,
            quantity: item.quantity,
            unitPrice: item.unitPrice,
          }),
        ),
      };
      createMutation.mutate(payload);
    } catch {
      // validation failed
    }
  };

  const getStatusActions = (record: PurchaseOrder) => {
    const items: { key: string; label: string; onClick: () => void; danger?: boolean }[] = [];
    const { status } = record;

    if (status === 'DRAFT') {
      items.push({
        key: 'submit',
        label: 'Submit',
        onClick: () => updateStatusMutation.mutate({ id: record.id, status: 'PENDING' }),
      });
    }
    if (status === 'PENDING') {
      items.push({
        key: 'approve',
        label: 'Approve',
        onClick: () => updateStatusMutation.mutate({ id: record.id, status: 'APPROVED' }),
      });
    }
    if (status === 'APPROVED') {
      items.push({
        key: 'receive',
        label: 'Mark Received',
        onClick: () => updateStatusMutation.mutate({ id: record.id, status: 'RECEIVED' }),
      });
    }
    if (status !== 'CANCELLED' && status !== 'RECEIVED') {
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
      onClick: () => purchaseOrdersApi.downloadPurchaseOrderPdf(record.id),
    });

    return items;
  };

  // ---- Computed ----

  const formItems = Form.useWatch('items', form) || [];
  const calculatedTotal = formItems.reduce(
    (sum: number, item: { quantity?: number; unitPrice?: number } | undefined) =>
      sum + ((item?.quantity || 0) * (item?.unitPrice || 0)),
    0,
  );

  // ---- Columns ----

  const columns = [
    {
      title: 'Order #',
      dataIndex: 'orderNumber',
      key: 'orderNumber',
      render: (orderNumber: string) => (
        <Tag color="blue" style={{ fontFamily: 'monospace' }}>
          {orderNumber}
        </Tag>
      ),
    },
    {
      title: 'Supplier',
      dataIndex: 'supplierName',
      key: 'supplierName',
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
        <Tag color={STATUS_COLORS[status] || 'default'}>{status}</Tag>
      ),
    },
    {
      title: 'Total Amount',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (amount: number) => `$${amount.toFixed(2)}`,
    },
    {
      title: 'Expected Delivery',
      dataIndex: 'expectedDelivery',
      key: 'expectedDelivery',
      render: (date: string | null) =>
        date ? dayjs(date).format('YYYY-MM-DD') : '\u2014',
    },
    {
      title: 'Created By',
      dataIndex: 'createdByName',
      key: 'createdByName',
      render: (name: string | null) => name || '\u2014',
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
      render: (_: unknown, record: PurchaseOrder) => {
        const actionItems = getStatusActions(record);
        return (
          <Space>
            {actionItems.length > 0 && (
              <Dropdown menu={{ items: actionItems }} trigger={['click']}>
                <Button size="small">
                  Actions <DownOutlined />
                </Button>
              </Dropdown>
            )}
            {record.status === 'DRAFT' && (
              <Popconfirm
                title="Delete purchase order"
                description="Are you sure you want to delete this purchase order?"
                onConfirm={() => deleteMutation.mutate(record.id)}
                okText="Yes"
                cancelText="No"
              >
                <Button type="link" danger size="small" icon={<DeleteOutlined />}>
                  Delete
                </Button>
              </Popconfirm>
            )}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[{ title: 'Home' }, { title: 'Purchase Orders' }]}
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
          Purchase Orders
        </Title>
        <Space>
          <Search
            placeholder="Search orders..."
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
              { value: 'DRAFT', label: 'Draft' },
              { value: 'PENDING', label: 'Pending' },
              { value: 'APPROVED', label: 'Approved' },
              { value: 'RECEIVED', label: 'Received' },
              { value: 'CANCELLED', label: 'Cancelled' },
            ]}
          />
          <Button icon={<DownloadOutlined />} onClick={handleExport}>Export</Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              form.resetFields();
              setModalOpen(true);
            }}
          >
            New Purchase Order
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
          showTotal: (total) => `Total ${total} orders`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title="New Purchase Order"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={closeModal}
        confirmLoading={createMutation.isPending}
        destroyOnClose
        width={720}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item
              name="supplierId"
              label="Supplier"
              rules={[{ required: true, message: 'Supplier is required' }]}
              style={{ flex: 1 }}
            >
              <Select
                placeholder="Select supplier"
                showSearch
                optionFilterProp="label"
                options={(suppliers || []).map((s) => ({
                  value: s.id,
                  label: s.name,
                }))}
              />
            </Form.Item>

            <Form.Item
              name="warehouseId"
              label="Warehouse"
              rules={[{ required: true, message: 'Warehouse is required' }]}
              style={{ flex: 1 }}
            >
              <Select
                placeholder="Select warehouse"
                showSearch
                optionFilterProp="label"
                options={(warehouses || []).map((w) => ({
                  value: w.id,
                  label: w.name,
                }))}
              />
            </Form.Item>
          </div>

          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item
              name="expectedDelivery"
              label="Expected Delivery"
              style={{ flex: 1 }}
            >
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item name="notes" label="Notes" style={{ flex: 1 }}>
              <TextArea rows={1} />
            </Form.Item>
          </div>

          <Typography.Text strong style={{ display: 'block', marginBottom: 8 }}>
            Items
          </Typography.Text>

          <Form.List name="items">
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...restField }) => (
                  <div
                    key={key}
                    style={{
                      display: 'flex',
                      gap: 8,
                      alignItems: 'flex-start',
                      marginBottom: 8,
                    }}
                  >
                    <Form.Item
                      {...restField}
                      name={[name, 'productId']}
                      rules={[{ required: true, message: 'Product is required' }]}
                      style={{ flex: 3, marginBottom: 0 }}
                    >
                      <Select
                        placeholder="Select product"
                        showSearch
                        optionFilterProp="label"
                        options={(productsData?.content || []).map((p) => ({
                          value: p.id,
                          label: `${p.sku} - ${p.name}`,
                        }))}
                      />
                    </Form.Item>

                    <Form.Item
                      {...restField}
                      name={[name, 'quantity']}
                      rules={[{ required: true, message: 'Required' }]}
                      style={{ flex: 1, marginBottom: 0 }}
                    >
                      <InputNumber
                        placeholder="Qty"
                        min={1}
                        style={{ width: '100%' }}
                      />
                    </Form.Item>

                    <Form.Item
                      {...restField}
                      name={[name, 'unitPrice']}
                      rules={[{ required: true, message: 'Required' }]}
                      style={{ flex: 1, marginBottom: 0 }}
                    >
                      <InputNumber
                        placeholder="Unit Price"
                        min={0}
                        step={0.01}
                        style={{ width: '100%' }}
                      />
                    </Form.Item>

                    <Button
                      type="text"
                      danger
                      icon={<MinusCircleOutlined />}
                      onClick={() => remove(name)}
                      style={{ marginTop: 4 }}
                    />
                  </div>
                ))}
                <Button
                  type="dashed"
                  onClick={() => add()}
                  block
                  icon={<PlusOutlined />}
                >
                  Add Item
                </Button>
              </>
            )}
          </Form.List>

          <div
            style={{
              marginTop: 16,
              textAlign: 'right',
              fontSize: 16,
              fontWeight: 600,
            }}
          >
            Total: ${calculatedTotal.toFixed(2)}
          </div>
        </Form>
      </Modal>
    </div>
  );
};

export default PurchaseOrders;
