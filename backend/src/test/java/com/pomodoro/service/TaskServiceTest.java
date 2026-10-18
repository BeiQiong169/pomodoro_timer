package com.pomodoro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import com.pomodoro.dto.task.UpdateTaskRequest;
import com.pomodoro.exception.NotFoundException;
import com.pomodoro.mapper.TaskMapper;
import com.pomodoro.model.TaskRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void updateOtherUsersTaskThrowsNotFound() {
        when(taskMapper.findByIdAndUserId(10L, 2L)).thenReturn(null);

        assertThatThrownBy(() -> taskService.update(2L, 10L, new UpdateTaskRequest("新标题", true)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("任务不存在");
    }

    @Test
    void deleteOtherUsersTaskThrowsNotFound() {
        when(taskMapper.deleteByIdAndUserId(10L, 2L)).thenReturn(0);

        assertThatThrownBy(() -> taskService.delete(2L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("任务不存在");
    }

    @Test
    void repeatedCompletionKeepsFirstCompletedAt() {
        TaskRow task = task(false, null);
        when(taskMapper.findByIdAndUserId(10L, 2L)).thenReturn(task);
        when(taskMapper.update(any(TaskRow.class))).thenReturn(1);

        taskService.update(2L, 10L, new UpdateTaskRequest(null, true));
        LocalDateTime firstCompletedAt = task.getCompletedAt();

        taskService.update(2L, 10L, new UpdateTaskRequest(null, true));

        assertThat(firstCompletedAt).isNotNull();
        assertThat(task.getCompletedAt()).isEqualTo(firstCompletedAt);
        verify(taskMapper, times(2)).update(task);
    }

    @Test
    void uncompletingClearsCompletedAt() {
        TaskRow task = task(true, LocalDateTime.now().minusHours(1));
        when(taskMapper.findByIdAndUserId(10L, 2L)).thenReturn(task);
        when(taskMapper.update(any(TaskRow.class))).thenReturn(1);

        taskService.update(2L, 10L, new UpdateTaskRequest(null, false));

        assertThat(task.isCompleted()).isFalse();
        assertThat(task.getCompletedAt()).isNull();
    }

    private TaskRow task(boolean completed, LocalDateTime completedAt) {
        TaskRow task = new TaskRow();
        task.setId(10L);
        task.setUserId(2L);
        task.setTitle("写代码");
        task.setCompleted(completed);
        task.setCompletedAt(completedAt);
        return task;
    }
}
