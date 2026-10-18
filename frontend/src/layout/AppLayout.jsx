import { useState } from 'react';
import { Button, Layout, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

const { Header, Content, Footer } = Layout;
const { Title } = Typography;

export default function AppLayout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [loggingOut, setLoggingOut] = useState(false);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await logout();
      navigate('/login', { replace: true });
    } finally {
      setLoggingOut(false);
    }
  };

  return (
    <Layout className="app-layout">
      <Header className="app-header">
        <Title level={4} style={{ color: '#fff', margin: 0 }}>
          🍅 番茄钟
        </Title>
        <Space>
          <span style={{ color: 'rgba(255, 255, 255, 0.85)' }}>{user?.username}</span>
          <Button onClick={handleLogout} loading={loggingOut}>
            退出
          </Button>
        </Space>
      </Header>
      <Content className="app-content">{children}</Content>
      <Footer style={{ textAlign: 'center' }}>番茄钟 · 专注记录</Footer>
    </Layout>
  );
}
