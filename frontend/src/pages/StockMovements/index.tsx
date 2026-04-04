import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Modal,
  Form,
  Select,
  Tag,
  InputNumber,
  Breadcrumb,
  Typography,
  Space,
  message,
} from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { inventoryApi } from '../../api/inventory';
import { warehousesApi } from '../../api/warehouses';
import type { InventoryItem } from '../../api/inventory';

const { Title } = Typography;
const { Search } = Input;

const StockMovements: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [warehouseId, setWarehouseId] = useState<number | undefined>(undefined);
  const [modalOpen, setModalOpen] = useState(false);
  const [adjustingItem, setAdjustingItem] = useState<InventoryItem | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['inventory', page, warehouseId, search],
    queryFn: () =>
      inventoryApi.getAll({
        page: page - 1,
        size: pageSize,
        warehouseId,
        search: search || undefined,
      }),
  });

  const { data: warehouses } = useQuery({
    queryKey: ['warehouses-active'],
    queryFn: () => warehousesApi.getAllActive(),
  });

  const adjustMutation = useMutation({
    mutationFn: (data: { productId: number; warehouseId: number; quantity: number; reason?: string }) =>
      inventoryApi.adjust(data),
    onSuccess: () => {
      message.success('Stock adjusted successfully');
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] });
      queryClient.invalidateQueries({ queryKey: ['low-stock'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to adjust stock');
    },
  });

  const openAdjustModal = (item: InventoryItem) => {
    setAdjustingItem(item);
    form.setFieldsValue({
      quantity: item.quantity,
      reason: '',
    });
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setAdjustingItem(null);
    form.resetFields();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (adjustingItem) {
        adjustMutation.mutate({
          productId: adjustingItem.productId,
          warehouseId: adjustingItem.warehouseId,
          quantity: values.quantity,
          reason: values.reason || undefined,
        });
      }
    } catch {
      // validation failed
    }
  };

  const columns = [
    {
      title: 'Product Name',
      dataIndex: 'productName',
      key: 'productName',
    },
    {
      title: 'SKU',
      dataIndex: 'productSku',
      key: 'productSku',
      render: (sku: string) => (
        <Tag style={{ fontFamily: 'monospace' }}>{sku}</Tag>
      ),
    },
    {
      title: 'Warehouse',
      dataIndex: 'warehouseName',
      key: 'warehouseName',
    },
    {
      title: 'Quantity',
      dataIndex: 'quantity',
      key: 'quantity',
      render: (qty: number, record: InventoryItem) => (
        <span style={{ color: record.lowStock ? '#ff4d4f' : undefined, fontWeight: record.lowStock ? 600 : undefined }}>
          {qty}
        </span>
      ),
    },
    {
      title: 'Reserved',
      dataIndex: 'reservedQuantity',
      key: 'reservedQuantity',
    },
    {
      title: 'Available',
      dataIndex: 'availableQuantity',
      key: 'availableQuantity',
    },
    {
      title: 'Reorder Point',
      dataIndex: 'reorderPoint',
      key: 'reorderPoint',
    },
    {
      title: 'Status',
      key: 'status',
      render: (_: unknown, record: InventoryItem) =>
        record.lowStock ? (
          <Tag color="red">Low Stock</Tag>
        ) : (
          <Tag color="green">In Stock</Tag>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: InventoryItem) => (
        <Button type="link" onClick={() => openAdjustModal(record)}>
          Adjust
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: 'Home' },
          { title: 'Inventory' },
        ]}
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
          Inventory Stock Levels
        </Title>
        <Space>
          <Select
            placeholder="All Warehouses"
            allowClear
            style={{ width: 200 }}
            onChange={(value) => {
              setWarehouseId(value);
              setPage(1);
            }}
            options={
              warehouses?.map((w) => ({
                value: w.id,
                label: w.name,
              })) ?? []
            }
          />
          <Search
            placeholder="Search products..."
            allowClear
            onSearch={(value) => {
              setSearch(value);
              setPage(1);
            }}
            style={{ width: 250 }}
          />
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
          showTotal: (total) => `Total ${total} items`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title="Adjust Stock"
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={closeModal}
        confirmLoading={adjustMutation.isPending}
        destroyOnClose
      >
        {adjustingItem && (
          <div style={{ marginBottom: 16 }}>
            <p>
              <strong>Product:</strong> {adjustingItem.productName}
            </p>
            <p>
              <strong>Warehouse:</strong> {adjustingItem.warehouseName}
            </p>
          </div>
        )}
        <Form form={form} layout="vertical" style={{ marginTop: 8 }}>
          <Form.Item
            name="quantity"
            label="New Quantity"
            rules={[{ required: true, message: 'Quantity is required' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="reason"
            label="Reason"
          >
            <Input placeholder="Reason for adjustment" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default StockMovements;
