import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  Checkbox,
  Empty,
  List,
  message,
  Popconfirm,
  Space,
  Spin,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import { createTask, deleteTask, listTasks, updateTask } from '../api/tasks.js';
import TaskFormModal from './TaskFormModal.jsx';

const { Text, Title } = Typography;

export default function TaskPanel({
  refreshToken = 0,
  selectedTaskId,
  onSelectTask,
  onClearSelectedTask,
  onTasksChanged,
}) {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loaded, setLoaded] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const loadTasks = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await listTasks();
      setTasks(data);
      setLoaded(true);
    } catch (err) {
      message.error(err.response?.data?.message || '加载任务失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTasks();
  }, [loadTasks, refreshToken]);

  useEffect(() => {
    if (loaded && selectedTaskId != null
      && !tasks.some((task) => task.id === selectedTaskId)) {
      onClearSelectedTask();
    }
  }, [loaded, tasks, selectedTaskId, onClearSelectedTask]);

  const openCreate = () => {
    setEditingTask(null);
    setModalOpen(true);
  };

  const openEdit = (task) => {
    setEditingTask(task);
    setModalOpen(true);
  };

  const closeModal = () => {
    if (!submitting) {
      setModalOpen(false);
    }
  };

  const handleSubmit = async ({ title }) => {
    setSubmitting(true);
    try {
      if (editingTask) {
        await updateTask(editingTask.id, { title });
        message.success('任务已更新');
      } else {
        await createTask({ title });
        message.success('任务已创建');
      }
      setModalOpen(false);
      onTasksChanged?.();
      await loadTasks();
    } catch (err) {
      message.error(err.response?.data?.message || '保存任务失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggle = async (task) => {
    try {
      await updateTask(task.id, { completed: !task.completed });
      onTasksChanged?.();
      await loadTasks();
    } catch (err) {
      message.error(err.response?.data?.message || '更新任务状态失败');
    }
  };

  const handleDelete = async (task) => {
    try {
      await deleteTask(task.id);
      if (task.id === selectedTaskId) {
        onClearSelectedTask();
      }
      message.success('任务已删除');
      onTasksChanged?.();
      await loadTasks();
    } catch (err) {
      message.error(err.response?.data?.message || '删除任务失败');
    }
  };

  const pendingTasks = tasks.filter((task) => !task.completed);
  const completedTasks = tasks.filter((task) => task.completed);

  const renderTask = (task) => {
    const actions = [];
    if (!task.completed && task.id !== selectedTaskId) {
      actions.push(
        <Button key="select" type="link" onClick={() => onSelectTask(task.id)}>
          设为当前
        </Button>,
      );
    }
    actions.push(
      <Button key="edit" type="link" onClick={() => openEdit(task)}>
        编辑
      </Button>,
    );
    actions.push(
      <Popconfirm
        key="delete"
        title="删除这个任务？"
        description="历史番茄统计会保留。"
        okText="删除"
        cancelText="取消"
        onConfirm={() => handleDelete(task)}
      >
        <Button type="link" danger>
          删除
        </Button>
      </Popconfirm>,
    );

    return (
      <List.Item key={task.id} actions={actions}>
        <Space wrap>
          <Checkbox
            checked={task.completed}
            onChange={() => handleToggle(task)}
            aria-label={task.title}
          />
          <Text delete={task.completed}>{task.title}</Text>
          {task.id === selectedTaskId && <Tag color="blue">当前</Tag>}
          <Tooltip title="已完成番茄数">
            <Tag>🍅 {task.pomodoroCount}</Tag>
          </Tooltip>
        </Space>
      </List.Item>
    );
  };

  return (
    <Card
      title="任务"
      extra={(
        <Button type="primary" onClick={openCreate}>
          新建任务
        </Button>
      )}
    >
      <Spin spinning={loading}>
        <Title level={5} style={{ marginTop: 0 }}>
          待办 ({pendingTasks.length})
        </Title>
        <List
          rowKey="id"
          dataSource={pendingTasks}
          renderItem={renderTask}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="暂无待办任务"
              />
            ),
          }}
        />

        <Title level={5} style={{ marginTop: 24 }}>
          已完成 ({completedTasks.length})
        </Title>
        <List
          rowKey="id"
          dataSource={completedTasks}
          renderItem={renderTask}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="暂无已完成任务"
              />
            ),
          }}
        />
      </Spin>

      <TaskFormModal
        open={modalOpen}
        task={editingTask}
        submitting={submitting}
        onCancel={closeModal}
        onSubmit={handleSubmit}
      />
    </Card>
  );
}
