import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, message, Alert, Steps } from 'antd';
import { MailOutlined, LockOutlined, KeyOutlined } from '@ant-design/icons';
import { authApi } from '../api/auth';

const { Title, Text } = Typography;

const ForgotPassword: React.FC = () => {
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [resetToken, setResetToken] = useState('');
  const [resetSuccess, setResetSuccess] = useState(false);
  const [forgotForm] = Form.useForm();
  const [resetForm] = Form.useForm();

  const onForgotSubmit = async (values: { email: string }) => {
    setLoading(true);
    try {
      const response = await authApi.forgotPassword(values.email);
      setResetToken(response.token);
      message.success(response.message);
      setStep(1);
      resetForm.setFieldsValue({ token: response.token });
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to process request.');
    } finally {
      setLoading(false);
    }
  };

  const onResetSubmit = async (values: { token: string; newPassword: string; confirmPassword: string }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      await authApi.resetPassword(values.token, values.newPassword);
      setResetSuccess(true);
      message.success('Password has been reset successfully!');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      message.error(error.response?.data?.message ?? 'Failed to reset password.');
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
          width: 420,
          boxShadow: '0 20px 60px rgba(0,0,0,0.3), 0 8px 24px rgba(0,0,0,0.15)',
          borderRadius: 12,
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
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
            Forgot Password
          </Title>
          <Text type="secondary">Reset your account password</Text>
        </div>

        <Steps
          current={step}
          size="small"
          style={{ marginBottom: 24 }}
          items={[
            { title: 'Verify Email' },
            { title: 'Reset Password' },
          ]}
        />

        {step === 0 && (
          <Form
            form={forgotForm}
            name="forgot-password"
            onFinish={onForgotSubmit}
            autoComplete="off"
            layout="vertical"
          >
            <Form.Item
              name="email"
              rules={[
                { required: true, message: 'Please enter your email' },
                { type: 'email', message: 'Please enter a valid email' },
              ]}
            >
              <Input
                prefix={<MailOutlined />}
                placeholder="Email address"
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
                Send Reset Link
              </Button>
            </Form.Item>
          </Form>
        )}

        {step === 1 && !resetSuccess && (
          <>
            <Alert
              message="Reset Token Generated"
              description={`In production this would be sent via email. Your token: ${resetToken}`}
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
            <Form
              form={resetForm}
              name="reset-password"
              onFinish={onResetSubmit}
              autoComplete="off"
              layout="vertical"
              initialValues={{ token: resetToken }}
            >
              <Form.Item
                name="token"
                rules={[{ required: true, message: 'Please enter the reset token' }]}
              >
                <Input
                  prefix={<KeyOutlined />}
                  placeholder="Reset token"
                  size="large"
                />
              </Form.Item>

              <Form.Item
                name="newPassword"
                rules={[
                  { required: true, message: 'Please enter a new password' },
                  { min: 6, message: 'Password must be at least 6 characters' },
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="New password"
                  size="large"
                />
              </Form.Item>

              <Form.Item
                name="confirmPassword"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: 'Please confirm your password' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('Passwords do not match'));
                    },
                  }),
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="Confirm new password"
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
                  Reset Password
                </Button>
              </Form.Item>
            </Form>
          </>
        )}

        {resetSuccess && (
          <Alert
            message="Password Reset Successful"
            description="Your password has been reset successfully. You can now log in with your new password."
            type="success"
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}

        <div style={{ textAlign: 'center' }}>
          <Text>
            Remember your password? <Link to="/login">Back to Login</Link>
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default ForgotPassword;
