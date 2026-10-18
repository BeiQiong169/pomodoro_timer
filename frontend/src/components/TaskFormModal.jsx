import { useEffect } from 'react';
import { Form, Input, Modal } from 'antd';

export default function TaskFormModal({ open, task, submitting, onCancel, onSubmit }) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (!open) {
      return;
    }
    if (task) {
      form.setFieldsValue({ title: task.title });
    } else {
      form.resetFields();
    }
  }, [open, task, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      await onSubmit(values);
    } catch {
      // 校验失败时保持弹窗打开。
    }
  };

  return (
    <Modal
      title={task ? '编辑任务' : '新建任务'}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      confirmLoading={submitting}
      okText="保存"
      cancelText="取消"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="title"
          label="任务标题"
          rules={[
            { required: true, whitespace: true, message: '请输入任务标题' },
            { max: 200, message: '任务标题不能超过 200 个字符' },
          ]}
        >
          <Input
            maxLength={200}
            placeholder="例如：写周报"
            autoFocus
            onPressEnter={handleOk}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
