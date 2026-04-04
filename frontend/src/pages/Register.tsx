import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';

const { Title, Text } = Typography;

interface RegisterFormValues {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

const Register: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);

  const onFinish = async (values: RegisterFormValues) => {
    setLoading(true);
    try {
      const response = await authApi.register({
        firstName: values.firstName,
        lastName: values.lastName,
        username: values.username,
        email: values.email,
        password: values.password,
      });
      setAuth(response.user, response.accessToken, response.refreshToken);
      message.success('Registration successful!');
      navigate('/dashboard');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%)',
      }}
    >
      <Card
        style={{
          width: 480,
          boxShadow: '0 20px 60px rgba(0,0,0,0.3), 0 8px 24px rgba(0,0,0,0.15)',
          borderRadius: 12,
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ marginBottom: 16 }}>
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="4" y="14" width="40" height="28" rx="3" stroke="#1677ff" strokeWidth="2.5" fill="none" />
              <path d="M4 20h40" stroke="#1677ff" strokeWidth="2.5" />
              <path d="M16 6h16l8 8H8l8-8z" stroke="#1677ff" strokeWidth="2.5" fill="rgba(22,119,255,0.15)" />
              <rect x="18" y="26" width="12" height="8" rx="1.5" stroke="#1677ff" strokeWidth="2" fill="rgba(22,119,255,0.15)" />
              <path d="M21 30h6" stroke="#1677ff" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <Title level={2} style={{ marginBottom: 4 }}>
            EaseManage
          </Title>
          <Text style={{ display: 'block', fontSize: 14, color: '#8c8c8c', marginBottom: 4 }}>
            Inventory Management System
          </Text>
          <Text type="secondary">Create a new account</Text>
        </div>

        <Form
          form={form}
          name="register"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
        >
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item
              name="firstName"
              rules={[{ required: true, message: 'First name is required' }]}
              style={{ flex: 1 }}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="First Name"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="lastName"
              rules={[{ required: true, message: 'Last name is required' }]}
              style={{ flex: 1 }}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="Last Name"
                size="large"
              />
            </Form.Item>
          </div>

          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Username is required' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="Username"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'Email is required' },
              { type: 'email', message: 'Please enter a valid email' },
            ]}
          >
            <Input
              prefix={<MailOutlined />}
              placeholder="Email"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: 'Password is required' },
              { min: 6, message: 'Password must be at least 6 characters' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Password"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={['password']}
            rules={[
              { required: true, message: 'Please confirm your password' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('Passwords do not match'));
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Confirm Password"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              size="large"
            >
              Register
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center' }}>
          <Text>
            Already have an account? <Link to="/login">Login</Link>
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default Register;
