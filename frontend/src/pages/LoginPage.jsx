import { useState } from 'react';
import { Alert, Button, Card, Form, Input, Spin, Typography } from 'antd';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

const { Title, Text } = Typography;

export default function LoginPage() {
  const { user, loading, login } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (loading) {
    return (
      <div className="page-center">
        <Spin size="large" />
      </div>
    );
  }

  if (user) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (values) => {
    setSubmitting(true);
    setError('');
    try {
      await login(values);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || '登录失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <Title level={2} style={{ textAlign: 'center', marginBottom: 4 }}>
          🍅 番茄钟
        </Title>
        <Text type="secondary">登录后开始专注</Text>

        {error && (
          <Alert type="error" message={error} showIcon style={{ marginTop: 16 }} />
        )}

        <Form
          layout="vertical"
          onFinish={handleSubmit}
          requiredMark={false}
          style={{ marginTop: 16 }}
        >
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input autoComplete="username" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={submitting}>
            登录
          </Button>
        </Form>

        <div style={{ marginTop: 16, textAlign: 'center' }}>
          还没有账号？ <Link to="/register">注册</Link>
        </div>
      </Card>
    </div>
  );
}
