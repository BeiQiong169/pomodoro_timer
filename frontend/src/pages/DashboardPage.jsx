import { useCallback, useState } from 'react';
import { Col, Row } from 'antd';
import StatsPanel from '../components/StatsPanel.jsx';
import TaskPanel from '../components/TaskPanel.jsx';
import TimerCard from '../components/TimerCard.jsx';
import { useSelectedTaskId } from '../hooks/useSelectedTaskId.js';

export default function DashboardPage() {
  const { selectedTaskId, setSelectedTaskId, clearSelectedTask } = useSelectedTaskId();
  const [tasksVersion, setTasksVersion] = useState(0);
  const [statsVersion, setStatsVersion] = useState(0);

  const handlePomodoroRecorded = useCallback(() => {
    setTasksVersion((version) => version + 1);
    setStatsVersion((version) => version + 1);
  }, []);

  const handleTasksChanged = useCallback(() => {
    setStatsVersion((version) => version + 1);
  }, []);

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={10}>
          <TimerCard
            selectedTaskId={selectedTaskId}
            onPomodoroRecorded={handlePomodoroRecorded}
          />
        </Col>
        <Col xs={24} lg={14}>
          <TaskPanel
            refreshToken={tasksVersion}
            selectedTaskId={selectedTaskId}
            onSelectTask={setSelectedTaskId}
            onClearSelectedTask={clearSelectedTask}
            onTasksChanged={handleTasksChanged}
          />
        </Col>
      </Row>
      <div style={{ marginTop: 16 }}>
        <StatsPanel refreshToken={statsVersion} />
      </div>
    </div>
  );
}
