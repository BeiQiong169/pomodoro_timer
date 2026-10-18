package com.pomodoro.dto.task;

import java.time.LocalDateTime;

import com.pomodoro.model.TaskRow;

public record TaskResponse(
        Long id,
        String title,
        boolean completed,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        long pomodoroCount) {

    public static TaskResponse from(TaskRow task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.isCompleted(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getPomodoroCount());
    }
}
