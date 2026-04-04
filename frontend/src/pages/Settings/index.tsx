import {
  Card,
  Form,
  Input,
  Select,
  InputNumber,
  Breadcrumb,
  Typography,
  Row,
  Col,
  Alert,
} from 'antd';

const { Title } = Typography;

const Settings: React.FC = () => {
  return (
    <div>
      <Breadcrumb
        items={[{ title: 'Home' }, { title: 'Settings' }]}
        style={{ marginBottom: 16 }}
      />
      <Title level={3} style={{ marginBottom: 24 }}>
        Settings
      </Title>

      <Alert
        message="Settings will be editable in a future update."
        type="info"
        showIcon
        style={{ marginBottom: 24 }}
      />

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={12}>
          <Card title="Company Profile">
            <Form layout="vertical">
              <Form.Item label="Company Name">
                <Input
                  placeholder="EaseManage Inc."
                  disabled
                />
              </Form.Item>
              <Form.Item label="Email">
                <Input
                  placeholder="admin@easemanage.com"
                  disabled
                />
              </Form.Item>
              <Form.Item label="Phone">
                <Input
                  placeholder="+1 (555) 123-4567"
                  disabled
                />
              </Form.Item>
              <Form.Item label="Address">
                <Input
                  placeholder="123 Business Ave, Suite 100"
                  disabled
                />
              </Form.Item>
            </Form>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="System Preferences">
            <Form layout="vertical">
              <Form.Item label="Default Currency">
                <Select
                  placeholder="USD"
                  disabled
                  options={[
                    { value: 'USD', label: 'USD - US Dollar' },
                    { value: 'EUR', label: 'EUR - Euro' },
                    { value: 'GBP', label: 'GBP - British Pound' },
                  ]}
                  defaultValue="USD"
                />
              </Form.Item>
              <Form.Item label="Items Per Page">
                <Select
                  placeholder="10"
                  disabled
                  options={[
                    { value: 10, label: '10' },
                    { value: 20, label: '20' },
                    { value: 50, label: '50' },
                  ]}
                  defaultValue={10}
                />
              </Form.Item>
              <Form.Item label="Low Stock Threshold">
                <InputNumber
                  style={{ width: '100%' }}
                  placeholder="10"
                  disabled
                  defaultValue={10}
                  min={1}
                />
              </Form.Item>
            </Form>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Settings;
