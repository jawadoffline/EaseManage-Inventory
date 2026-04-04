import { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { notificationsApi } from '../api/notifications';
import type { Notification as NotifType } from '../api/notifications';
import { useThemeStore } from '../store/themeStore';
import GlobalSearch from '../components/GlobalSearch';
import {
  Layout,
  Menu,
  Avatar,
  Dropdown,
  Badge,
  Typography,
  Drawer,
  theme,
} from 'antd';
import {
  DashboardOutlined,
  ShoppingOutlined,
  AppstoreOutlined,
  HomeOutlined,
  TeamOutlined,
  ShoppingCartOutlined,
  FileTextOutlined,
  SwapOutlined,
  BarChartOutlined,
  ContactsOutlined,
  UserOutlined,
  SettingOutlined,
  BellOutlined,
  SunOutlined,
  MoonOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import type { Role } from '../types';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

interface MenuItem {
  key: string;
  icon: React.ReactNode;
  label: string;
  roles: Role[];
}

const allMenuItems: MenuItem[] = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: 'Dashboard', roles: ['ADMIN', 'MANAGER', 'WAREHOUSE_STAFF', 'VIEWER'] },
  { key: '/products', icon: <ShoppingOutlined />, label: 'Products', roles: ['ADMIN', 'MANAGER', 'WAREHOUSE_STAFF', 'VIEWER'] },
  { key: '/categories', icon: <AppstoreOutlined />, label: 'Categories', roles: ['ADMIN', 'MANAGER', 'WAREHOUSE_STAFF', 'VIEWER'] },
  { key: '/warehouses', icon: <HomeOutlined />, label: 'Warehouses', roles: ['ADMIN', 'MANAGER'] },
  { key: '/suppliers', icon: <TeamOutlined />, label: 'Suppliers', roles: ['ADMIN', 'MANAGER'] },
  { key: '/customers', icon: <ContactsOutlined />, label: 'Customers', roles: ['ADMIN', 'MANAGER'] },
  { key: '/purchase-orders', icon: <ShoppingCartOutlined />, label: 'Purchase Orders', roles: ['ADMIN', 'MANAGER', 'WAREHOUSE_STAFF'] },
  { key: '/sales-orders', icon: <FileTextOutlined />, label: 'Sales Orders', roles: ['ADMIN', 'MANAGER', 'WAREHOUSE_STAFF'] },
  { key: '/stock-movements', icon: <SwapOutlined />, label: 'Stock Movements', roles: ['ADMIN', 'MANAGER', 'WAREHOUSE_STAFF'] },
  { key: '/reports', icon: <BarChartOutlined />, label: 'Reports', roles: ['ADMIN', 'MANAGER', 'VIEWER'] },
  { key: '/audit-trail', icon: <FileTextOutlined />, label: 'Audit Trail', roles: ['ADMIN'] },
  { key: '/users', icon: <UserOutlined />, label: 'Users', roles: ['ADMIN'] },
  { key: '/settings', icon: <SettingOutlined />, label: 'Settings', roles: ['ADMIN'] },
];

const NotificationBell: React.FC = () => {
  const queryClient = useQueryClient();
  const { data: countData } = useQuery({
    queryKey: ['notifications-count'],
    queryFn: () => notificationsApi.getUnreadCount(),
    refetchInterval: 30000,
  });
  const { data: notifData } = useQuery({
    queryKey: ['notifications-recent'],
    queryFn: () => notificationsApi.getAll({ page: 0, size: 5 }),
    refetchInterval: 30000,
  });

  const handleMarkAllRead = async () => {
    await notificationsApi.markAllAsRead();
    queryClient.invalidateQueries({ queryKey: ['notifications-count'] });
    queryClient.invalidateQueries({ queryKey: ['notifications-recent'] });
  };

  const items: MenuProps['items'] = [
    ...(notifData?.content ?? []).map((n: NotifType) => ({
      key: String(n.id),
      label: (
        <div style={{ maxWidth: 280 }}>
          <div style={{ fontWeight: n.isRead ? 'normal' : 'bold', fontSize: 13 }}>{n.title}</div>
          <div style={{ fontSize: 12, color: '#888', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{n.message}</div>
        </div>
      ),
    })),
    ...(notifData?.content?.length ? [{ type: 'divider' as const }] : []),
    {
      key: 'mark-all',
      label: <span style={{ fontSize: 12, color: '#1677ff' }}>Mark all as read</span>,
      onClick: handleMarkAllRead,
    },
  ];

  return (
    <Dropdown menu={{ items }} placement="bottomRight" trigger={['click']}>
      <Badge count={countData?.count ?? 0} size="small">
        <BellOutlined style={{ fontSize: 18, cursor: 'pointer' }} />
      </Badge>
    </Dropdown>
  );
};

const AppLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      if (mobile) setCollapsed(true);
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const navigate = useNavigate();
  const location = useLocation();
  const { user, refreshToken, logout } = useAuthStore();
  const { token } = theme.useToken();
  const { darkMode, toggleDarkMode } = useThemeStore();

  const userRole = user?.role ?? 'VIEWER';

  const menuItems: MenuProps['items'] = allMenuItems
    .filter((item) => item.roles.includes(userRole))
    .map((item) => ({
      key: item.key,
      icon: item.icon,
      label: item.label,
    }));

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key);
    if (isMobile) setCollapsed(true);
  };

  const handleLogout = async () => {
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch {
      // ignore logout API errors
    } finally {
      logout();
      navigate('/login');
    }
  };

  const userDropdownItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profile',
      onClick: () => navigate('/profile'),
    },
    { type: 'divider' },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: handleLogout,
    },
  ];

  const selectedKey = '/' + location.pathname.split('/').filter(Boolean)[0];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {isMobile ? (
        <Drawer
          placement="left"
          open={!collapsed}
          onClose={() => setCollapsed(true)}
          width={250}
          closable={false}
          styles={{ body: { padding: 0, background: '#001529' } }}
        >
          <div
            style={{
              height: 64,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 10,
              borderBottom: '1px solid rgba(255,255,255,0.1)',
              padding: '0 12px',
            }}
          >
            <svg width="24" height="24" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ flexShrink: 0 }}>
              <rect x="4" y="14" width="40" height="28" rx="3" stroke="#1677ff" strokeWidth="3" fill="none" />
              <path d="M4 20h40" stroke="#1677ff" strokeWidth="3" />
              <path d="M16 6h16l8 8H8l8-8z" stroke="#1677ff" strokeWidth="3" fill="rgba(22,119,255,0.2)" />
              <rect x="18" y="26" width="12" height="8" rx="1.5" stroke="#1677ff" strokeWidth="2.5" fill="rgba(22,119,255,0.2)" />
              <path d="M21 30h6" stroke="#1677ff" strokeWidth="2.5" strokeLinecap="round" />
            </svg>
            <Text
              strong
              style={{
                color: '#fff',
                fontSize: 18,
                whiteSpace: 'nowrap',
                fontFamily: "'Segoe UI', -apple-system, sans-serif",
                letterSpacing: '-0.3px',
              }}
            >
              EaseManage
            </Text>
          </div>
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[selectedKey]}
            items={menuItems}
            onClick={handleMenuClick}
          />
        </Drawer>
      ) : (
        <Sider
          collapsible
          collapsed={collapsed}
          onCollapse={setCollapsed}
          width={250}
          collapsedWidth={80}
          theme="dark"
          style={{
            overflow: 'auto',
            height: '100vh',
            position: 'fixed',
            left: 0,
            top: 0,
            bottom: 0,
            zIndex: 10,
          }}
        >
          <div
            style={{
              height: 64,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 10,
              borderBottom: '1px solid rgba(255,255,255,0.1)',
              padding: '0 12px',
            }}
          >
            <svg width="24" height="24" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ flexShrink: 0 }}>
              <rect x="4" y="14" width="40" height="28" rx="3" stroke="#1677ff" strokeWidth="3" fill="none" />
              <path d="M4 20h40" stroke="#1677ff" strokeWidth="3" />
              <path d="M16 6h16l8 8H8l8-8z" stroke="#1677ff" strokeWidth="3" fill="rgba(22,119,255,0.2)" />
              <rect x="18" y="26" width="12" height="8" rx="1.5" stroke="#1677ff" strokeWidth="2.5" fill="rgba(22,119,255,0.2)" />
              <path d="M21 30h6" stroke="#1677ff" strokeWidth="2.5" strokeLinecap="round" />
            </svg>
            {!collapsed && (
              <Text
                strong
                style={{
                  color: '#fff',
                  fontSize: 18,
                  whiteSpace: 'nowrap',
                  fontFamily: "'Segoe UI', -apple-system, sans-serif",
                  letterSpacing: '-0.3px',
                }}
              >
                EaseManage
              </Text>
            )}
          </div>
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[selectedKey]}
            items={menuItems}
            onClick={handleMenuClick}
          />
        </Sider>
      )}

      <Layout style={{ marginLeft: isMobile ? 0 : (collapsed ? 80 : 250), transition: 'margin-left 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: token.colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: `1px solid ${token.colorBorderSecondary}`,
            position: 'sticky',
            top: 0,
            zIndex: 9,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 16, flex: '0 0 auto' }}>
            {collapsed ? (
              <MenuUnfoldOutlined
                style={{ fontSize: 18, cursor: 'pointer' }}
                onClick={() => setCollapsed(false)}
              />
            ) : (
              <MenuFoldOutlined
                style={{ fontSize: 18, cursor: 'pointer' }}
                onClick={() => setCollapsed(true)}
              />
            )}
            <Text strong style={{ fontSize: 16 }}>
              EaseManage
            </Text>
          </div>

          <div style={{ flex: '0 1 320px', padding: '12px 0' }}>
            <GlobalSearch />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 20, flex: '0 0 auto' }}>
            {darkMode ? (
              <SunOutlined style={{ fontSize: 18, cursor: 'pointer', color: '#faad14' }} onClick={toggleDarkMode} />
            ) : (
              <MoonOutlined style={{ fontSize: 18, cursor: 'pointer' }} onClick={toggleDarkMode} />
            )}
            <NotificationBell />
            <Dropdown menu={{ items: userDropdownItems }} placement="bottomRight">
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                <Avatar size="small" icon={<UserOutlined />} />
                <Text>{user?.firstName ?? 'User'} {user?.lastName ?? ''}</Text>
              </div>
            </Dropdown>
          </div>
        </Header>

        <Content
          style={{
            margin: 24,
            padding: 24,
            background: token.colorBgContainer,
            borderRadius: token.borderRadiusLG,
            minHeight: 280,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AppLayout;
