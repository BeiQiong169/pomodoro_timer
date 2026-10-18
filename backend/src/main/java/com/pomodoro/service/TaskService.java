package com.pomodoro.service;

import java.time.LocalDateTime;
import java.util.List;

import com.pomodoro.dto.task.CreateTaskRequest;
import com.pomodoro.dto.task.TaskResponse;
import com.pomodoro.dto.task.UpdateTaskRequest;
import com.pomodoro.exception.NotFoundException;
import com.pomodoro.mapper.TaskMapper;
import com.pomodoro.model.TaskRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public List<TaskResponse> list(Long userId) {
        return taskMapper.findAllByUserId(userId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional
    public TaskResponse create(Long userId, CreateTaskRequest request) {
        TaskRow task = new TaskRow();
        task.setUserId(userId);
        task.setTitle(request.title());
        task.setCompleted(false);
        taskMapper.insert(task);
        return TaskResponse.from(requireTask(task.getId(), userId));
    }

    @Transactional
    public TaskResponse update(Long userId, Long taskId, UpdateTaskRequest request) {
        TaskRow task = requireTask(taskId, userId);

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.completed() != null) {
            if (request.completed() && !task.isCompleted()) {
                task.setCompleted(true);
                task.setCompletedAt(LocalDateTime.now());
            } else if (!request.completed()) {
                task.setCompleted(false);
                task.setCompletedAt(null);
            }
        }

        if (taskMapper.update(task) == 0) {
            throw new NotFoundException("任务不存在");
        }
        return TaskResponse.from(requireTask(taskId, userId));
    }

    @Transactional
    public void delete(Long userId, Long taskId) {
        if (taskMapper.deleteByIdAndUserId(taskId, userId) == 0) {
            throw new NotFoundException("任务不存在");
        }
    }

    private TaskRow requireTask(Long taskId, Long userId) {
        TaskRow task = taskMapper.findByIdAndUserId(taskId, userId);
        if (task == null) {
            throw new NotFoundException("任务不存在");
        }
        return task;
    }
}
