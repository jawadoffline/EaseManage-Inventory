import {
  Row,
  Col,
  Card,
  Statistic,
  Breadcrumb,
  Typography,
  Table,
  Tag,
  Tabs,
  Spin,
  Button,
} from 'antd';
import { theme } from 'antd';
import {
  DollarOutlined,
  ShoppingOutlined,
  InboxOutlined,
  DownloadOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  ResponsiveContainer,
} from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { reportsApi } from '../../api/reports';

const { Title } = Typography;

const PIE_COLORS = ['#52c41a', '#faad14', '#ff4d4f', '#1890ff', '#722ed1', '#13c2c2'];

const formatMoney = (value: number) => `$${value.toLocaleString()}`;

const downloadCsv = (headers: string[], rows: string[][], filename: string) => {
  const csvContent = [
    headers.join(','),
    ...rows.map((row) =>
      row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(',')
    ),
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
};

/* ──────────────────────── Tab 1: Inventory Valuation ──────────────────────── */

const InventoryValuationTab: React.FC = () => {
  const { data, isLoading } = useQuery({
    queryKey: ['report-valuation'],
    queryFn: () => reportsApi.getInventoryValuation(),
  });

  const handleExportCsv = () => {
    if (!data?.items) return;
    const headers = [
      'Product Name',
      'SKU',
      'Category',
      'Quantity',
      'Cost Price',
      'Selling Price',
      'Total Cost Value',
      'Total Retail Value',
    ];
    const rows = data.items.map((item) => [
      item.productName,
      item.sku,
      item.categoryName,
      String(item.quantity),
      String(item.costPrice),
      String(item.sellingPrice),
      String(item.totalCostValue),
      String(item.totalRetailValue),
    ]);
    downloadCsv(headers, rows, 'inventory-valuation.csv');
  };

  const columns = [
    {
      title: 'Product Name',
      dataIndex: 'productName',
      key: 'productName',
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
      title: 'Category',
      dataIndex: 'categoryName',
      key: 'categoryName',
    },
    {
      title: 'Quantity',
      dataIndex: 'quantity',
      key: 'quantity',
      align: 'right' as const,
    },
    {
      title: 'Cost Price',
      dataIndex: 'costPrice',
      key: 'costPrice',
      align: 'right' as const,
      render: (v: number) => formatMoney(v),
    },
    {
      title: 'Selling Price',
      dataIndex: 'sellingPrice',
      key: 'sellingPrice',
      align: 'right' as const,
      render: (v: number) => formatMoney(v),
    },
    {
      title: 'Total Cost Value',
      dataIndex: 'totalCostValue',
      key: 'totalCostValue',
      align: 'right' as const,
      render: (v: number) => formatMoney(v),
    },
    {
      title: 'Total Retail Value',
      dataIndex: 'totalRetailValue',
      key: 'totalRetailValue',
      align: 'right' as const,
      render: (v: number) => formatMoney(v),
    },
  ];

  const potentialProfit = (data?.totalRetailValue ?? 0) - (data?.totalCostValue ?? 0);

  return (
    <Spin spinning={isLoading}>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="Total Cost Value"
              value={data?.totalCostValue ?? 0}
              prefix={<DollarOutlined />}
              valueStyle={{ color: '#1890ff' }}
              formatter={(v) => formatMoney(Number(v))}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="Total Retail Value"
              value={data?.totalRetailValue ?? 0}
              prefix={<DollarOutlined />}
              valueStyle={{ color: '#52c41a' }}
              formatter={(v) => formatMoney(Number(v))}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="Potential Profit"
              value={potentialProfit}
              prefix={<DollarOutlined />}
              valueStyle={{ color: '#722ed1' }}
              formatter={(v) => formatMoney(Number(v))}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="Total Units"
              value={data?.totalUnits ?? 0}
              prefix={<InboxOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="Inventory Items"
        extra={
          <Button
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleExportCsv}
            disabled={!data?.items?.length}
          >
            Export CSV
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={data?.items ?? []}
          rowKey="productId"
          pagination={{ pageSize: 10, showSizeChanger: true }}
          scroll={{ x: 900 }}
        />
      </Card>
    </Spin>
  );
};

/* ──────────────────────── Tab 2: Stock Summary ──────────────────────── */

const useChartTheme = () => {
  const { token } = theme.useToken();
  return {
    tooltipStyle: {
      borderRadius: 8,
      border: `1px solid ${token.colorBorderSecondary}`,
      boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
      backgroundColor: token.colorBgElevated,
      color: token.colorText,
    },
    axisColor: token.colorTextSecondary,
    gridColor: token.colorBorderSecondary,
  };
};

const StockSummaryTab: React.FC = () => {
  const { tooltipStyle, axisColor, gridColor } = useChartTheme();
  const { data, isLoading } = useQuery({
    queryKey: ['report-stock'],
    queryFn: () => reportsApi.getStockSummary(),
  });

  const stockDistribution = [
    { name: 'In Stock', value: data?.inStockCount ?? 0 },
    { name: 'Low Stock', value: data?.lowStockCount ?? 0 },
    { name: 'Out of Stock', value: data?.outOfStockCount ?? 0 },
  ];

  const STOCK_COLORS = ['#52c41a', '#faad14', '#ff4d4f'];

  return (
    <Spin spinning={isLoading}>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="Total Products"
              value={data?.totalProducts ?? 0}
              prefix={<ShoppingOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="In Stock"
              value={data?.inStockCount ?? 0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="Low Stock"
              value={data?.lowStockCount ?? 0}
              prefix={<WarningOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable>
            <Statistic
              title="Out of Stock"
              value={data?.outOfStockCount ?? 0}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Stock Distribution" styles={{ body: { paddingTop: 24 } }}>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={stockDistribution}
                  cx="50%"
                  cy="50%"
                  innerRadius={65}
                  outerRadius={105}
                  paddingAngle={4}
                  dataKey="value"
                  label={({ name, percent }: { name?: string; percent?: number }) =>
                    `${name ?? ''} ${((percent ?? 0) * 100).toFixed(0)}%`
                  }
                  labelLine={{ strokeWidth: 1 }}
                >
                  {stockDistribution.map((_entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={STOCK_COLORS[index % STOCK_COLORS.length]}
                      stroke="none"
                    />
                  ))}
                </Pie>
                <Tooltip contentStyle={tooltipStyle} />
                <Legend verticalAlign="bottom" iconType="circle" iconSize={10} />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Stock by Category" styles={{ body: { paddingTop: 24 } }}>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={data?.byCategory ?? []} barSize={40}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={gridColor} />
                <XAxis dataKey="categoryName" tick={{ fontSize: 12, fill: axisColor }} />
                <YAxis tick={{ fontSize: 12, fill: axisColor }} />
                <Tooltip contentStyle={tooltipStyle} />
                <Bar
                  dataKey="totalQuantity"
                  fill="#1890ff"
                  name="Total Quantity"
                  radius={[6, 6, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </Spin>
  );
};

/* ──────────────────────── Tab 3: Order Summary ──────────────────────── */

const OrderSummaryTab: React.FC = () => {
  const { tooltipStyle } = useChartTheme();
  const { data, isLoading } = useQuery({
    queryKey: ['report-orders'],
    queryFn: () => reportsApi.getOrderSummary(),
  });

  const purchaseStatusData = Object.entries(data?.purchaseByStatus ?? {}).map(
    ([name, value]) => ({ name, value })
  );

  const salesStatusData = Object.entries(data?.salesByStatus ?? {}).map(
    ([name, value]) => ({ name, value })
  );

  const renderStatusPie = (chartData: { name: string; value: number }[]) => (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          innerRadius={55}
          outerRadius={90}
          paddingAngle={4}
          dataKey="value"
          label={({ name, percent }: { name?: string; percent?: number }) =>
            `${name ?? ''} ${((percent ?? 0) * 100).toFixed(0)}%`
          }
          labelLine={{ strokeWidth: 1 }}
        >
          {chartData.map((_entry, index) => (
            <Cell
              key={`cell-${index}`}
              fill={PIE_COLORS[index % PIE_COLORS.length]}
              stroke="none"
            />
          ))}
        </Pie>
        <Tooltip contentStyle={tooltipStyle} />
        <Legend verticalAlign="bottom" iconType="circle" iconSize={10} />
      </PieChart>
    </ResponsiveContainer>
  );

  return (
    <Spin spinning={isLoading}>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Purchase Orders" styles={{ body: { paddingTop: 24 } }}>
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={12}>
                <Statistic
                  title="Total Orders"
                  value={data?.totalPurchaseOrders ?? 0}
                  prefix={<FileTextOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="Total Value"
                  value={data?.totalPurchaseValue ?? 0}
                  prefix={<DollarOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                  formatter={(v) => formatMoney(Number(v))}
                />
              </Col>
            </Row>
            {renderStatusPie(purchaseStatusData)}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Sales Orders" styles={{ body: { paddingTop: 24 } }}>
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={12}>
                <Statistic
                  title="Total Orders"
                  value={data?.totalSalesOrders ?? 0}
                  prefix={<FileTextOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="Total Value"
                  value={data?.totalSalesValue ?? 0}
                  prefix={<DollarOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                  formatter={(v) => formatMoney(Number(v))}
                />
              </Col>
            </Row>
            {renderStatusPie(salesStatusData)}
          </Card>
        </Col>
      </Row>
    </Spin>
  );
};

/* ──────────────────────── Reports Page ──────────────────────── */

const Reports: React.FC = () => {
  const tabItems = [
    {
      key: 'valuation',
      label: 'Inventory Valuation',
      children: <InventoryValuationTab />,
    },
    {
      key: 'stock',
      label: 'Stock Summary',
      children: <StockSummaryTab />,
    },
    {
      key: 'orders',
      label: 'Order Summary',
      children: <OrderSummaryTab />,
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[{ title: 'Home' }, { title: 'Reports' }]}
        style={{ marginBottom: 16 }}
      />
      <Title level={3} style={{ marginBottom: 24 }}>
        Reports
      </Title>

      <Tabs items={tabItems} defaultActiveKey="valuation" />
    </div>
  );
};

export default Reports;
