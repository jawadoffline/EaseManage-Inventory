import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Modal,
  Form,
  Select,
  Tag,
  Switch,
  Breadcrumb,
  Typography,
  Space,
  Row,
  Col,
  InputNumber,
  message,
  Popconfirm,
  Avatar,
  Upload,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  BarcodeOutlined,
  ShoppingOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import ProductImportExport from '../../components/ProductImportExport';
import BarcodeLabel from '../../components/BarcodeLabel';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productsApi } from '../../api/products';
import { categoriesApi } from '../../api/categories';
import type { Product, ProductRequest } from '../../api/products';
import type { Category } from '../../api/categories';
import dayjs from 'dayjs';

const { Title } = Typography;
const { Search } = Input;
const { TextArea } = Input;

const UNIT_OPTIONS = [
  { value: 'PCS', label: 'PCS' },
  { value: 'KG', label: 'KG' },
  { value: 'LTR', label: 'LTR' },
  { value: 'MTR', label: 'MTR' },
  { value: 'BOX', label: 'BOX' },
  { value: 'PACK', label: 'PACK' },
];

const Products: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [isActive, setIsActive] = useState<boolean | undefined>(undefined);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [barcodeProduct, setBarcodeProduct] = useState<Product | null>(null);
  const [uploadProduct, setUploadProduct] = useState<Product | null>(null);
  const [uploading, setUploading] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ['products', { page, pageSize, search, categoryId, isActive }],
    queryFn: () =>
      productsApi.getAll({
        page: page - 1,
        size: pageSize,
        search: search || undefined,
        categoryId,
        isActive,
      }),
  });

  const { data: categories } = useQuery({
    queryKey: ['categories', 'flat'],
    queryFn: () => categoriesApi.getAllFlat(),
  });

  const createMutation = useMutation({
    mutationFn: (data: ProductRequest) => productsApi.create(data),
    onSuccess: () => {
      message.success('Product created successfully');
      queryClient.invalidateQueries({ queryKey: ['products'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to create product');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ProductRequest }) =>
      productsApi.update(id, data),
    onSuccess: () => {
      message.success('Product updated successfully');
      queryClient.invalidateQueries({ queryKey: ['products'] });
      closeModal();
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to update product');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => productsApi.delete(id),
    onSuccess: () => {
      message.success('Product deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to delete product');
    },
  });

  const handleImageUpload = async (file: File) => {
    if (!uploadProduct) return;
    setUploading(true);
    try {
      await productsApi.uploadImage(uploadProduct.id, file);
      message.success('Image uploaded successfully');
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setUploadProduct(null);
    } catch {
      message.error('Failed to upload image');
    } finally {
      setUploading(false);
    }
  };

  const handleImageDelete = async (product: Product) => {
    try {
      await productsApi.deleteImage(product.id);
      message.success('Image removed successfully');
      queryClient.invalidateQueries({ queryKey: ['products'] });
    } catch {
      message.error('Failed to remove image');
    }
  };

  const openCreateModal = () => {
    setEditingProduct(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEditModal = (product: Product) => {
    setEditingProduct(product);
    form.setFieldsValue({
      name: product.name,
      description: product.description,
      categoryId: product.categoryId,
      costPrice: product.costPrice,
      sellingPrice: product.sellingPrice,
      unitOfMeasure: product.unitOfMeasure,
      barcode: product.barcode,
      minStockLevel: product.minStockLevel,
      maxStockLevel: product.maxStockLevel,
      reorderPoint: product.reorderPoint,
      isActive: product.isActive,
    });
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingProduct(null);
    form.resetFields();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload: ProductRequest = {
        name: values.name,
        description: values.description,
        categoryId: values.categoryId ?? null,
        costPrice: values.costPrice,
        sellingPrice: values.sellingPrice,
        unitOfMeasure: values.unitOfMeasure,
        barcode: values.barcode,
        minStockLevel: values.minStockLevel,
        maxStockLevel: values.maxStockLevel,
        reorderPoint: values.reorderPoint,
        isActive: values.isActive,
      };
      if (editingProduct) {
        updateMutation.mutate({ id: editingProduct.id, data: payload });
      } else {
        createMutation.mutate(payload);
      }
    } catch {
      // validation failed
    }
  };

  const columns = [
    {
      title: 'Image',
      dataIndex: 'imageUrl',
      key: 'imageUrl',
      width: 60,
      render: (imageUrl: string | null) => (
        <Avatar
          shape="square"
          size={40}
          src={imageUrl ? 'http://localhost:8080' + imageUrl : undefined}
          icon={!imageUrl ? <ShoppingOutlined /> : undefined}
        />
      ),
    },
    {
      title: 'SKU',
      dataIndex: 'sku',
      key: 'sku',
      render: (sku: string) => (
        <Tag style={{ fontFamily: 'monospace' }}>{sku}</Tag>
      ),
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Category',
      dataIndex: 'categoryName',
      key: 'categoryName',
      render: (name: string | null) => name ?? '\u2014',
    },
    {
      title: 'Cost Price',
      dataIndex: 'costPrice',
      key: 'costPrice',
      render: (price: number) => `$${price.toFixed(2)}`,
    },
    {
      title: 'Selling Price',
      dataIndex: 'sellingPrice',
      key: 'sellingPrice',
      render: (price: number) => `$${price.toFixed(2)}`,
    },
    {
      title: 'Unit',
      dataIndex: 'unitOfMeasure',
      key: 'unitOfMeasure',
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (active: boolean) =>
        active ? (
          <Tag color="green">Active</Tag>
        ) : (
          <Tag color="red">Inactive</Tag>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Product) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => openEditModal(record)}
          >
            Edit
          </Button>
          <Button
            type="link"
            icon={<UploadOutlined />}
            onClick={() => setUploadProduct(record)}
          >
            Image
          </Button>
          <Button
            type="link"
            icon={<BarcodeOutlined />}
            onClick={() => setBarcodeProduct(record)}
          >
            Barcode
          </Button>
          <Popconfirm
            title="Delete product"
            description="Are you sure you want to delete this product?"
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

  const categoryOptions = (categories ?? []).map((cat: Category) => ({
    value: cat.id,
    label: cat.name,
  }));

  return (
    <div>
      <Breadcrumb
        items={[
          { title: 'Home' },
          { title: 'Products' },
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
          Products
        </Title>
        <Space>
          <Search
            placeholder="Search products..."
            allowClear
            onSearch={(value) => {
              setSearch(value);
              setPage(1);
            }}
            style={{ width: 250 }}
          />
          <Select
            placeholder="Category"
            allowClear
            options={categoryOptions}
            onChange={(value) => {
              setCategoryId(value);
              setPage(1);
            }}
            style={{ width: 160 }}
          />
          <Space>
            <span>Active only</span>
            <Switch
              checked={isActive === true}
              onChange={(checked) => {
                setIsActive(checked ? true : undefined);
                setPage(1);
              }}
            />
          </Space>
          <ProductImportExport />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            Add Product
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
          showTotal: (total) => `Total ${total} products`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title={editingProduct ? 'Edit Product' : 'Add Product'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={closeModal}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        destroyOnClose
        width={640}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: 'Product name is required' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item name="description" label="Description">
            <TextArea rows={3} />
          </Form.Item>

          <Form.Item name="categoryId" label="Category">
            <Select
              allowClear
              placeholder="Select category"
              options={categoryOptions}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="costPrice" label="Cost Price">
                <InputNumber
                  min={0}
                  precision={2}
                  prefix="$"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="sellingPrice" label="Selling Price">
                <InputNumber
                  min={0}
                  precision={2}
                  prefix="$"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="unitOfMeasure" label="Unit of Measure">
                <Select
                  placeholder="Select unit"
                  options={UNIT_OPTIONS}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="barcode" label="Barcode">
                <Input />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="minStockLevel" label="Min Stock Level">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="maxStockLevel" label="Max Stock Level">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="reorderPoint" label="Reorder Point">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          {editingProduct && (
            <Form.Item name="isActive" label="Active" valuePropName="checked">
              <Switch />
            </Form.Item>
          )}
        </Form>
      </Modal>

      <Modal
        title="Barcode Label"
        open={barcodeProduct !== null}
        onCancel={() => setBarcodeProduct(null)}
        footer={null}
        destroyOnClose
      >
        {barcodeProduct && (
          <BarcodeLabel
            sku={barcodeProduct.sku}
            name={barcodeProduct.name}
            barcode={barcodeProduct.barcode || barcodeProduct.sku}
          />
        )}
      </Modal>

      <Modal
        title={`Upload Image - ${uploadProduct?.name ?? ''}`}
        open={uploadProduct !== null}
        onCancel={() => setUploadProduct(null)}
        footer={uploadProduct?.imageUrl ? [
          <Popconfirm
            key="remove"
            title="Remove this image?"
            onConfirm={() => {
              if (uploadProduct) handleImageDelete(uploadProduct);
              setUploadProduct(null);
            }}
            okText="Yes"
            cancelText="No"
          >
            <Button danger>Remove Image</Button>
          </Popconfirm>,
        ] : null}
        destroyOnClose
      >
        {uploadProduct && (
          <div style={{ textAlign: 'center' }}>
            {uploadProduct.imageUrl && (
              <div style={{ marginBottom: 16 }}>
                <img
                  src={'http://localhost:8080' + uploadProduct.imageUrl}
                  alt={uploadProduct.name}
                  style={{ maxWidth: '100%', maxHeight: 200, objectFit: 'contain' }}
                />
              </div>
            )}
            <Upload.Dragger
              accept="image/*"
              showUploadList={false}
              beforeUpload={(file) => {
                handleImageUpload(file);
                return false;
              }}
              disabled={uploading}
            >
              <p className="ant-upload-drag-icon">
                <UploadOutlined style={{ fontSize: 32, color: '#1890ff' }} />
              </p>
              <p className="ant-upload-text">
                {uploading ? 'Uploading...' : 'Click or drag an image to upload'}
              </p>
              <p className="ant-upload-hint">
                Supports JPG, PNG, GIF. Max 5MB.
              </p>
            </Upload.Dragger>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default Products;
