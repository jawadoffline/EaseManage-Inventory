import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Modal,
  Form,
  Switch,
  Badge,
  Breadcrumb,
  Typography,
  Space,
  Row,
  Col,
  message,
  Popconfirm,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customersApi } from '../../api/customers';
import type { Customer, CustomerRequest } from '../../api/customers';
import { exportToCsv } from '../../utils/csvExport';
import CsvImportButton from '../../components/CsvImportButton';
import dayjs from 'dayjs';

const { Title } = Typography;
const { Search } = Input;
const { TextArea } = Input;

const Customers: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['customers', { page, pageSize, search }],
    queryFn: () =>
      customersApi.getAll({
        page: page - 1,
        size: pageSize,
        search: search || undefined,
      }),
  });

  const createMutation = useMutation({
    mutationFn: (data: CustomerRequest) => customersApi.create(data),
    onSuccess: () => {
      message.success('Customer created successfully');
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to create customer');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CustomerRequest }) =>
      customersApi.update(id, data),
    onSuccess: () => {
      message.success('Customer updated successfully');
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to update customer');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => customersApi.delete(id),
    onSuccess: () => {
      message.success('Customer deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to delete customer');
    },
  });

  const handleExport = () => {
    if (!data?.content) return;
    exportToCsv('customers.csv',
      ['Name', 'Email', 'Phone', 'Contact Person', 'City', 'Country', 'Status', 'Created At'],
      data.content.map((c: Customer) => [
        c.name, c.email, c.phone, c.contactPerson, c.city, c.country,
        c.isActive ? 'Active' : 'Inactive', dayjs(c.createdAt).format('YYYY-MM-DD'),
      ])
    );
  };

  const openCreateModal = () => {
    setEditingCustomer(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEditModal = (customer: Customer) => {
    setEditingCustomer(customer);
    form.setFieldsValue({
      name: customer.name,
      contactPerson: customer.contactPerson,
      email: customer.email,
      phone: customer.phone,
      address: customer.address,
      city: customer.city,
      country: customer.country,
      notes: customer.notes,
      isActive: customer.isActive,
    });
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingCustomer(null);
    form.resetFields();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingCustomer) {
        updateMutation.mutate({
          id: editingCustomer.id,
          data: {
            name: values.name,
            contactPerson: values.contactPerson,
            email: values.email,
            phone: values.phone,
            address: values.address,
            city: values.city,
            country: values.country,
            notes: values.notes,
            isActive: values.isActive,
          },
        });
      } else {
        createMutation.mutate({
          name: values.name,
          contactPerson: values.contactPerson,
          email: values.email,
          phone: values.phone,
          address: values.address,
          city: values.city,
          country: values.country,
          notes: values.notes,
        });
      }
    } catch {
      // validation failed
    }
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Contact Person',
      dataIndex: 'contactPerson',
      key: 'contactPerson',
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Phone',
      dataIndex: 'phone',
      key: 'phone',
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
      render: (_: unknown, record: Customer) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => openEditModal(record)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Delete customer"
            description="Are you sure you want to delete this customer?"
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
          { title: 'Customers' },
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
          Customers
        </Title>
        <Space>
          <Search
            placeholder="Search customers..."
            allowClear
            onSearch={(value) => {
              setSearch(value);
              setPage(1);
            }}
            style={{ width: 250 }}
          />
          <Button icon={<DownloadOutlined />} onClick={handleExport}>Export</Button>
          <CsvImportButton
            endpoint="/import/customers"
            templateHeaders="Name,Email,Phone,Contact Person,City,Country"
            onSuccess={() => queryClient.invalidateQueries({ queryKey: ['customers'] })}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            Add Customer
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
          showTotal: (total) => `Total ${total} customers`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title={editingCustomer ? 'Edit Customer' : 'Add Customer'}
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

          <Form.Item name="contactPerson" label="Contact Person">
            <Input />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[{ type: 'email', message: 'Please enter a valid email' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item name="phone" label="Phone">
            <Input />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="address" label="Address">
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="city" label="City">
                <Input />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="country" label="Country">
            <Input />
          </Form.Item>

          <Form.Item name="notes" label="Notes">
            <TextArea rows={3} />
          </Form.Item>

          {editingCustomer && (
            <Form.Item name="isActive" label="Active" valuePropName="checked">
              <Switch />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default Customers;
