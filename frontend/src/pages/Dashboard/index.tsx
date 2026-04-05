import { Row, Col, Card, Statistic, Breadcrumb, Typography, Table, Tag, Empty, Spin, theme } from 'antd';
import {
  ShoppingOutlined,
  WarningOutlined,
  HomeOutlined,
  AppstoreOutlined,
} from '@ant-design/icons';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { inventoryApi } from '../../api/inventory';
import type { InventoryItem } from '../../api/inventory';
import { useThemeStore } from '../../store/themeStore';
import { DashboardSkeleton } from '../../components/PageSkeleton';

const { Title } = Typography;

const PIE_COLORS = ['#faad14', '#1890ff', '#52c41a', '#ff4d4f', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16'];

const Dashboard: React.FC = () => {
  const { darkMode } = useThemeStore();
  const { token } = theme.useToken();

  // Dark-mode aware icon backgrounds
  const iconBg = (lightColor: string, darkColor: string) =>
    darkMode ? darkColor : lightColor;

  const tooltipStyle = {
    borderRadius: 8,
    border: `1px solid ${token.colorBorderSecondary}`,
    boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
    backgroundColor: token.colorBgElevated,
    color: token.colorText,
  };

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => inventoryApi.getDashboardStats(),
  });

  const { data: lowStock, isLoading: lowStockLoading } = useQuery({
    queryKey: ['low-stock'],
    queryFn: () => inventoryApi.getLowStock(),
  });

  const { data: chartData } = useQuery({
    queryKey: ['dashboard-charts'],
    queryFn: () => inventoryApi.getChartData(),
  });

  if (statsLoading && lowStockLoading) return <DashboardSkeleton />;

  const lowStockColumns = [
    {
      title: 'Product',
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
      render: (qty: number, record: InventoryItem) =>
        record.lowStock ? <Tag color="red">{qty}</Tag> : qty,
    },
    {
      title: 'Reorder Point',
      dataIndex: 'reorderPoint',
      key: 'reorderPoint',
    },
    {
      title: 'Status',
      key: 'status',
      render: () => <Tag color="red">Low Stock</Tag>,
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: 'Home' },
          { title: 'Dashboard' },
        ]}
        style={{ marginBottom: 16 }}
      />
      <Title level={3} style={{ marginBottom: 24 }}>
        Dashboard
      </Title>

      <Spin spinning={statsLoading}>
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable style={{ borderLeft: '4px solid #1890ff' }}>
              <Statistic
                title="Total Products"
                value={stats?.totalProducts ?? 0}
                prefix={
                  <div style={{
                    width: 40, height: 40, borderRadius: '50%',
                    background: iconBg('#e6f7ff', 'rgba(24,144,255,0.15)'), display: 'inline-flex',
                    alignItems: 'center', justifyContent: 'center', marginRight: 4,
                  }}>
                    <ShoppingOutlined style={{ fontSize: 20, color: '#1890ff' }} />
                  </div>
                }
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable style={{ borderLeft: '4px solid #ff4d4f' }}>
              <Statistic
                title="Low Stock Alerts"
                value={stats?.lowStockAlerts ?? 0}
                prefix={
                  <div style={{
                    width: 40, height: 40, borderRadius: '50%',
                    background: iconBg('#fff2f0', 'rgba(255,77,79,0.15)'), display: 'inline-flex',
                    alignItems: 'center', justifyContent: 'center', marginRight: 4,
                  }}>
                    <WarningOutlined style={{ fontSize: 20, color: '#ff4d4f' }} />
                  </div>
                }
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable style={{ borderLeft: '4px solid #52c41a' }}>
              <Statistic
                title="Warehouses"
                value={stats?.totalWarehouses ?? 0}
                prefix={
                  <div style={{
                    width: 40, height: 40, borderRadius: '50%',
                    background: iconBg('#f6ffed', 'rgba(82,196,26,0.15)'), display: 'inline-flex',
                    alignItems: 'center', justifyContent: 'center', marginRight: 4,
                  }}>
                    <HomeOutlined style={{ fontSize: 20, color: '#52c41a' }} />
                  </div>
                }
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable style={{ borderLeft: '4px solid #722ed1' }}>
              <Statistic
                title="Categories"
                value={stats?.totalCategories ?? 0}
                prefix={
                  <div style={{
                    width: 40, height: 40, borderRadius: '50%',
                    background: iconBg('#f9f0ff', 'rgba(114,46,209,0.15)'), display: 'inline-flex',
                    alignItems: 'center', justifyContent: 'center', marginRight: 4,
                  }}>
                    <AppstoreOutlined style={{ fontSize: 20, color: '#722ed1' }} />
                  </div>
                }
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
        </Row>
      </Spin>

      <Card title="Low Stock Alerts" style={{ marginBottom: 24 }}>
        {!lowStockLoading && (!lowStock || lowStock.length === 0) ? (
          <Empty description="No low stock alerts" />
        ) : (
          <Table
            columns={lowStockColumns}
            dataSource={lowStock ?? []}
            rowKey="id"
            loading={lowStockLoading}
            pagination={false}
          />
        )}
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card
            title="Stock by Category"
            styles={{ body: { paddingTop: 24 } }}
          >
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData?.stockByCategory ?? []} barSize={40}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={token.colorBorderSecondary} />
                <XAxis dataKey="name" tick={{ fontSize: 12, fill: token.colorTextSecondary }} />
                <YAxis tick={{ fontSize: 12, fill: token.colorTextSecondary }} />
                <Tooltip
                  contentStyle={tooltipStyle}
                />
                <Bar
                  dataKey="stock"
                  fill="#1890ff"
                  radius={[6, 6, 0, 0]}
                  background={{ fill: darkMode ? 'rgba(255,255,255,0.04)' : '#f5f5f5', radius: [6, 6, 0, 0] } as object}
                />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card
            title="Order Status"
            styles={{ body: { paddingTop: 24 } }}
          >
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={chartData?.orderStatus ?? []}
                  cx="50%"
                  cy="50%"
                  innerRadius={65}
                  outerRadius={105}
                  paddingAngle={4}
                  dataKey="value"
                  label={({ name, percent }: { name: string; percent?: number }) =>
                    `${name} ${((percent ?? 0) * 100).toFixed(0)}%`
                  }
                  labelLine={{ strokeWidth: 1, stroke: token.colorTextSecondary }}
                >
                  {(chartData?.orderStatus ?? []).map((_entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={PIE_COLORS[index % PIE_COLORS.length]}
                      stroke="none"
                    />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={tooltipStyle}
                />
                <Legend
                  verticalAlign="bottom"
                  iconType="circle"
                  iconSize={10}
                />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
