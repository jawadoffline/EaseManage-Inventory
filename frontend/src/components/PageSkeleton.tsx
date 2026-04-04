import { Skeleton, Card, Row, Col, Space } from 'antd';

export function DashboardSkeleton() {
  return (
    <div>
      <Skeleton.Input active style={{ width: 200, marginBottom: 24 }} />
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {[1, 2, 3, 4].map((i) => (
          <Col xs={24} sm={12} lg={6} key={i}>
            <Card>
              <Skeleton active paragraph={{ rows: 1 }} />
            </Card>
          </Col>
        ))}
      </Row>
      <Card>
        <Skeleton active paragraph={{ rows: 5 }} />
      </Card>
    </div>
  );
}

export function TableSkeleton() {
  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Skeleton.Input active style={{ width: 200 }} />
        <Skeleton.Button active />
      </Space>
      <Card>
        <Skeleton active paragraph={{ rows: 8 }} />
      </Card>
    </div>
  );
}
