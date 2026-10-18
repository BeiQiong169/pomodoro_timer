import { useEffect, useState } from 'react';
import { Button, Card, Space, Tag, Typography } from 'antd';
import { listTasks } from '../api/tasks.js';
import { LONG_BREAK_EVERY, PHASES } from '../constants/timer.js';
import { usePomodoroTimer } from '../hooks/usePomodoroTimer.js';

const { Text } = Typography;

const PHASE_COLORS = {
  WORK: 'red',
  SHORT_BREAK: 'green',
  LONG_BREAK: 'blue',
};

function formatTime(ms) {
  const totalSeconds = Math.ceil(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

export default function TimerCard({ selectedTaskId, onPomodoroRecorded }) {
  const { timer, start, pause, resume, reset } = usePomodoroTimer(
    selectedTaskId,
    onPomodoroRecorded,
  );
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    let cancelled = false;
    listTasks()
      .then(({ data }) => {
        if (!cancelled) {
          setTasks(data);
        }
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [selectedTaskId]);

  const displayTaskId = timer.phase === 'WORK'
    ? (timer.status === 'IDLE' ? selectedTaskId : timer.taskId)
    : null;
  const displayTask = tasks.find((task) => task.id === displayTaskId);
  const phase = PHASES[timer.phase];

  let taskText = '休息阶段';
  if (timer.phase === 'WORK') {
    taskText = displayTask ? `当前任务：${displayTask.title}` : '未选择任务';
  }

  return (
    <Card title="计时器">
      <div style={{ textAlign: 'center' }}>
        <Space size="middle">
          <Tag color={PHASE_COLORS[timer.phase]}>{phase.label}</Tag>
          <Text type="secondary">
            本轮 {timer.completedInCycle}/{LONG_BREAK_EVERY}
          </Text>
        </Space>
      </div>

      <div className="timer-display">{formatTime(timer.remainingMs)}</div>

      <div style={{ textAlign: 'center', marginBottom: 24 }}>
        <Text type={displayTask ? undefined : 'secondary'}>{taskText}</Text>
      </div>

      <div style={{ textAlign: 'center' }}>
        <Space>
          {timer.status === 'IDLE' && (
            <Button type="primary" size="large" onClick={start}>
              {timer.phase === 'WORK' ? '开始专注' : '开始休息'}
            </Button>
          )}
          {timer.status === 'RUNNING' && (
            <Button size="large" onClick={pause}>
              暂停
            </Button>
          )}
          {timer.status === 'PAUSED' && (
            <Button type="primary" size="large" onClick={resume}>
              继续
            </Button>
          )}
          {timer.status !== 'IDLE' && (
            <Button size="large" onClick={reset}>
              重置
            </Button>
          )}
        </Space>
      </div>
    </Card>
  );
}
