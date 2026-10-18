import { useEffect, useState } from 'react';
import {
  Card,
  Col,
  Empty,
  message,
  Row,
  Spin,
  Statistic,
  Typography,
} from 'antd';
import { getStatsSummary } from '../api/stats.js';
import WeeklyBarChart from './WeeklyBarChart.jsx';

const { Title } = Typography;

export default function StatsPanel({ refreshToken = 0 }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getStatsSummary()
      .then(({ data }) => {
        if (!cancelled) {
          setStats(data);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          message.error(err.response?.data?.message || '加载统计失败');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [refreshToken]);

  return (
    <Card title="统计">
      <Spin spinning={loading}>
        {stats ? (
          <>
            <Row gutter={[16, 16]}>
              <Col xs={24} sm={8}>
                <Statistic title="今日番茄" value={stats.todayPomodoroCount} />
              </Col>
              <Col xs={24} sm={8}>
                <Statistic
                  title="今日专注"
                  value={stats.todayFocusMinutes}
                  suffix="分钟"
                />
              </Col>
              <Col xs={24} sm={8}>
                <Statistic
                  title="今日完成"
                  value={stats.todayCompletedTaskCount}
                  suffix="个"
                />
              </Col>
            </Row>

            <Title level={5} style={{ marginTop: 24 }}>
              近 7 天
            </Title>
            <WeeklyBarChart data={stats.last7Days} />
          </>
        ) : (
          !loading && (
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description="暂无统计数据"
            />
          )
        )}
      </Spin>
    </Card>
  );
}
