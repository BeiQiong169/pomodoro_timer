package com.pomodoro.dto.session;

import java.time.LocalDateTime;

import com.pomodoro.model.PomodoroSessionRow;

public record PomodoroSessionResponse(
        Long id,
        Long taskId,
        int durationMinutes,
        LocalDateTime completedAt) {

    public static PomodoroSessionResponse from(PomodoroSessionRow session) {
        return new PomodoroSessionResponse(
                session.getId(),
                session.getTaskId(),
                session.getDurationMinutes(),
                session.getCompletedAt());
    }
}
