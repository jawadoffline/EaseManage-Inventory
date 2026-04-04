import { useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import {
  Card,
  Form,
  Input,
  Breadcrumb,
  Typography,
  Row,
  Col,
  Button,
  Avatar,
  Tag,
  App,
} from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { usersApi } from '../../api/users';
import { useAuthStore } from '../../store/authStore';

const { Title } = Typography;

const Profile: React.FC = () => {
  const { message } = App.useApp();
  const { user: authUser, accessToken, refreshToken, setAuth } = useAuthStore();
  const [profileForm] = Form.useForm();
  const [passwordForm] = Form.useForm();

  const { data: currentUser } = useQuery({
    queryKey: ['user-me'],
    queryFn: () => usersApi.getMe(),
  });

  useEffect(() => {
    if (currentUser) {
      profileForm.setFieldsValue({
        firstName: currentUser.firstName,
        lastName: currentUser.lastName,
        email: currentUser.email,
        username: currentUser.username,
        role: currentUser.role,
      });
    }
  }, [currentUser, profileForm]);

  const updateProfileMutation = useMutation({
    mutationFn: (values: { firstName: string; lastName: string; email: string }) =>
      usersApi.updateProfile(values),
    onSuccess: (updatedUser) => {
      if (accessToken && refreshToken) {
        setAuth(
          {
            id: updatedUser.id,
            username: updatedUser.username,
            email: updatedUser.email,
            firstName: updatedUser.firstName,
            lastName: updatedUser.lastName,
            role: updatedUser.role,
          },
          accessToken,
          refreshToken,
        );
      }
      message.success('Profile updated successfully');
    },
    onError: () => {
      message.error('Failed to update profile');
    },
  });

  const changePasswordMutation = useMutation({
    mutationFn: (values: { currentPassword: string; newPassword: string }) =>
      usersApi.changePassword(values.currentPassword, values.newPassword),
    onSuccess: () => {
      message.success('Password changed successfully');
      passwordForm.resetFields();
    },
    onError: () => {
      message.error('Failed to change password. Please check your current password.');
    },
  });

  const handleProfileSubmit = (values: { firstName: string; lastName: string; email: string }) => {
    updateProfileMutation.mutate(values);
  };

  const handlePasswordSubmit = (values: { currentPassword: string; newPassword: string }) => {
    changePasswordMutation.mutate(values);
  };

  return (
    <div>
      <Breadcrumb
        items={[{ title: 'Home' }, { title: 'Profile' }]}
        style={{ marginBottom: 16 }}
      />
      <Title level={3} style={{ marginBottom: 24 }}>
        My Profile
      </Title>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={12}>
          <Card title="Profile Information">
            <div style={{ textAlign: 'center', marginBottom: 24 }}>
              <Avatar size={80} icon={<UserOutlined />} />
            </div>
            <Form
              form={profileForm}
              layout="vertical"
              onFinish={handleProfileSubmit}
            >
              <Form.Item
                label="First Name"
                name="firstName"
                rules={[{ required: true, message: 'Please enter your first name' }]}
              >
                <Input placeholder="First Name" />
              </Form.Item>
              <Form.Item
                label="Last Name"
                name="lastName"
                rules={[{ required: true, message: 'Please enter your last name' }]}
              >
                <Input placeholder="Last Name" />
              </Form.Item>
              <Form.Item
                label="Email"
                name="email"
                rules={[
                  { required: true, message: 'Please enter your email' },
                  { type: 'email', message: 'Please enter a valid email' },
                ]}
              >
                <Input placeholder="Email" />
              </Form.Item>
              <Form.Item label="Username" name="username">
                <Input disabled />
              </Form.Item>
              <Form.Item label="Role" name="role">
                <div>
                  <Tag color="blue">{authUser?.role}</Tag>
                </div>
              </Form.Item>
              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={updateProfileMutation.isPending}
                >
                  Save Changes
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="Change Password">
            <Form
              form={passwordForm}
              layout="vertical"
              onFinish={handlePasswordSubmit}
            >
              <Form.Item
                label="Current Password"
                name="currentPassword"
                rules={[{ required: true, message: 'Please enter your current password' }]}
              >
                <Input.Password placeholder="Current Password" />
              </Form.Item>
              <Form.Item
                label="New Password"
                name="newPassword"
                rules={[
                  { required: true, message: 'Please enter a new password' },
                  { min: 6, message: 'Password must be at least 6 characters' },
                ]}
              >
                <Input.Password placeholder="New Password" />
              </Form.Item>
              <Form.Item
                label="Confirm Password"
                name="confirmPassword"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: 'Please confirm your new password' },
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
                <Input.Password placeholder="Confirm Password" />
              </Form.Item>
              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={changePasswordMutation.isPending}
                >
                  Change Password
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Profile;
