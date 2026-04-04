import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  InputNumber,
  Modal,
  Form,
  Tag,
  Badge,
  Switch,
  Breadcrumb,
  Typography,
  Space,
  message,
  Popconfirm,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import { exportToCsv } from '../../utils/csvExport';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { warehousesApi } from '../../api/warehouses';
import type { Warehouse, WarehouseRequest } from '../../api/warehouses';
import dayjs from 'dayjs';

const { Title } = Typography;
const { Search } = Input;

const Warehouses: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['warehouses', { page, pageSize, search }],
    queryFn: () =>
      warehousesApi.getAll({
        page: page - 1,
        size: pageSize,
        search: search || undefined,
      }),
  });

  const createMutation = useMutation({
    mutationFn: (data: WarehouseRequest) => warehousesApi.create(data),
    onSuccess: () => {
      message.success('Warehouse created successfully');
      queryClient.invalidateQueries({ queryKey: ['warehouses'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to create warehouse');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: WarehouseRequest }) =>
      warehousesApi.update(id, data),
    onSuccess: () => {
      message.success('Warehouse updated successfully');
      queryClient.invalidateQueries({ queryKey: ['warehouses'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to update warehouse');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => warehousesApi.delete(id),
    onSuccess: () => {
      message.success('Warehouse deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['warehouses'] });
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to delete warehouse');
    },
  });

  const openCreateModal = () => {
    setEditingWarehouse(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEditModal = (warehouse: Warehouse) => {
    setEditingWarehouse(warehouse);
    form.setFieldsValue({
      name: warehouse.name,
      code: warehouse.code,
      address: warehouse.address,
      city: warehouse.city,
      state: warehouse.state,
      country: warehouse.country,
      capacity: warehouse.capacity,
      isActive: warehouse.isActive,
    });
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingWarehouse(null);
    form.resetFields();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload: WarehouseRequest = {
        name: values.name,
        code: values.code.toUpperCase(),
        address: values.address,
        city: values.city,
        state: values.state,
        country: values.country,
        capacity: values.capacity,
        ...(editingWarehouse ? { isActive: values.isActive } : {}),
      };
      if (editingWarehouse) {
        updateMutation.mutate({ id: editingWarehouse.id, data: payload });
      } else {
        createMutation.mutate(payload);
      }
    } catch {
      // validation failed
    }
  };

  const handleExport = () => {
    const rows = (data?.content ?? []).map((item: Warehouse) => [
      item.name,
      item.code,
      item.city,
      item.state,
      item.country,
      item.capacity,
      item.isActive ? 'Active' : 'Inactive',
      item.createdAt ? dayjs(item.createdAt).format('YYYY-MM-DD HH:mm') : '',
    ]);
    exportToCsv('warehouses.csv', ['Name', 'Code', 'City', 'State', 'Country', 'Capacity', 'Status', 'Created At'], rows);
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Code',
      dataIndex: 'code',
      key: 'code',
      render: (code: string) => <Tag color="blue">{code}</Tag>,
    },
    {
      title: 'City',
      dataIndex: 'city',
      key: 'city',
    },
    {
      title: 'Country',
      dataIndex: 'country',
      key: 'country',
    },
    {
      title: 'Capacity',
      dataIndex: 'capacity',
      key: 'capacity',
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive: boolean) => (
        <Badge
          status={isActive ? 'success' : 'error'}
          text={isActive ? 'Active' : 'Inactive'}
        />
      ),
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
      render: (_: unknown, record: Warehouse) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => openEditModal(record)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Delete warehouse"
            description="Are you sure you want to delete this warehouse?"
            onConfirm={() => deleteMutation.mutate(record.id)}
            okText="Yes"
            cancelText="No"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              Delete
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: 'Home' },
          { title: 'Warehouses' },
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
          Warehouses
        </Title>
        <Space>
          <Search
            placeholder="Search warehouses..."
            allowClear
            onSearch={(value) => {
              setSearch(value);
              setPage(1);
            }}
            style={{ width: 250 }}
          />
          <Button icon={<DownloadOutlined />} onClick={handleExport}>Export</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            Add Warehouse
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
          showTotal: (total) => `Total ${total} warehouses`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title={editingWarehouse ? 'Edit Warehouse' : 'Add Warehouse'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={closeModal}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: 'Name is required' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="code"
            label="Code"
            rules={[{ required: true, message: 'Code is required' }]}
            normalize={(value: string) => value?.toUpperCase()}
          >
            <Input style={{ textTransform: 'uppercase' }} />
          </Form.Item>

          <Form.Item
            name="address"
            label="Address"
          >
            <Input />
          </Form.Item>

          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item
              name="city"
              label="City"
              style={{ flex: 1 }}
            >
              <Input />
            </Form.Item>

            <Form.Item
              name="state"
              label="State"
              style={{ flex: 1 }}
            >
              <Input />
            </Form.Item>
          </div>

          <Form.Item
            name="country"
            label="Country"
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="capacity"
            label="Capacity"
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          {editingWarehouse && (
            <Form.Item
              name="isActive"
              label="Active"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default Warehouses;
