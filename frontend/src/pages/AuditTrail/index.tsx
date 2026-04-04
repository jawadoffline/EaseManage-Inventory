import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Modal,
  Select,
  Tag,
  Breadcrumb,
  Typography,
  Space,
} from 'antd';
import { EyeOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../../api/audit';
import type { AuditLog } from '../../api/audit';
import dayjs from 'dayjs';

const { Title } = Typography;
const { Search } = Input;

const ENTITY_TYPE_COLORS: Record<string, string> = {
  Product: 'blue',
  Category: 'purple',
  Warehouse: 'cyan',
  Supplier: 'orange',
  PurchaseOrder: 'green',
  SalesOrder: 'green',
  User: 'red',
  Inventory: 'geekblue',
};

const ACTION_COLORS: Record<string, string> = {
  CREATE: 'green',
  UPDATE: 'blue',
  DELETE: 'red',
};

const ENTITY_TYPE_OPTIONS = [
  'Product',
  'Category',
  'Warehouse',
  'Supplier',
  'PurchaseOrder',
  'SalesOrder',
  'User',
  'Inventory',
].map((v) => ({ value: v, label: v }));

const ACTION_OPTIONS = ['CREATE', 'UPDATE', 'DELETE'].map((v) => ({
  value: v,
  label: v,
}));

const AuditTrail: React.FC = () => {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [entityType, setEntityType] = useState<string | undefined>(undefined);
  const [action, setAction] = useState<string | undefined>(undefined);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['audit', page, entityType, action, search],
    queryFn: () =>
      auditApi.getAll({
        page: page - 1,
        size: pageSize,
        entityType: entityType || undefined,
        action: action || undefined,
        search: search || undefined,
      }),
  });

  const formatJson = (value: string | null): string => {
    if (!value) return 'N/A';
    try {
      return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      return value;
    }
  };

  const openDetailModal = (record: AuditLog) => {
    setSelectedLog(record);
    setDetailModalOpen(true);
  };

  const columns = [
    {
      title: 'Timestamp',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: 'User',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: 'Entity Type',
      dataIndex: 'entityType',
      key: 'entityType',
      render: (type: string) => (
        <Tag color={ENTITY_TYPE_COLORS[type] ?? 'default'}>{type}</Tag>
      ),
    },
    {
      title: 'Entity ID',
      dataIndex: 'entityId',
      key: 'entityId',
    },
    {
      title: 'Action',
      dataIndex: 'action',
      key: 'action',
      render: (act: string) => (
        <Tag color={ACTION_COLORS[act] ?? 'default'}>{act}</Tag>
      ),
    },
    {
      title: 'Details',
      key: 'details',
      render: (_: unknown, record: AuditLog) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          onClick={() => openDetailModal(record)}
        >
          View
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[{ title: 'Home' }, { title: 'Audit Trail' }]}
        style={{ marginBottom: 16 }}
      />

      <Title level={3} style={{ marginBottom: 24 }}>
        Audit Trail
      </Title>

      <Space style={{ marginBottom: 24 }} wrap>
        <Search
          placeholder="Search..."
          allowClear
          onSearch={(value) => {
            setSearch(value);
            setPage(1);
          }}
          style={{ width: 250 }}
        />
        <Select
          placeholder="Entity Type"
          allowClear
          options={ENTITY_TYPE_OPTIONS}
          value={entityType}
          onChange={(value) => {
            setEntityType(value);
            setPage(1);
          }}
          style={{ width: 180 }}
        />
        <Select
          placeholder="Action"
          allowClear
          options={ACTION_OPTIONS}
          value={action}
          onChange={(value) => {
            setAction(value);
            setPage(1);
          }}
          style={{ width: 140 }}
        />
      </Space>

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
          showTotal: (total) => `Total ${total} records`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title="Audit Log Details"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalOpen(false)}>
            Close
          </Button>,
        ]}
        width={720}
        destroyOnClose
      >
        {selectedLog && (
          <div>
            <Title level={5}>Old Values</Title>
            <pre
              style={{
                background: '#f5f5f5',
                padding: 12,
                borderRadius: 6,
                maxHeight: 300,
                overflow: 'auto',
              }}
            >
              {formatJson(selectedLog.oldValues)}
            </pre>

            <Title level={5} style={{ marginTop: 16 }}>
              New Values
            </Title>
            <pre
              style={{
                background: '#f5f5f5',
                padding: 12,
                borderRadius: 6,
                maxHeight: 300,
                overflow: 'auto',
              }}
            >
              {formatJson(selectedLog.newValues)}
            </pre>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AuditTrail;
