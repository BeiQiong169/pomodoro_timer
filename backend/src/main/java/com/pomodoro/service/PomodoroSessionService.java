package com.pomodoro.service;

import java.time.LocalDateTime;

import com.pomodoro.dto.session.CreatePomodoroSessionRequest;
import com.pomodoro.dto.session.PomodoroSessionResponse;
import com.pomodoro.mapper.PomodoroSessionMapper;
import com.pomodoro.mapper.TaskMapper;
import com.pomodoro.model.PomodoroSessionRow;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PomodoroSessionService {

    private final PomodoroSessionMapper sessionMapper;
    private final TaskMapper taskMapper;

    public PomodoroSessionService(PomodoroSessionMapper sessionMapper, TaskMapper taskMapper) {
        this.sessionMapper = sessionMapper;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public PomodoroSessionResponse record(Long userId, CreatePomodoroSessionRequest request) {
        Long taskId = request.taskId();
        if (taskId != null && taskMapper.findByIdAndUserId(taskId, userId) == null) {
            taskId = null;
        }

        PomodoroSessionRow session = new PomodoroSessionRow();
        session.setUserId(userId);
        session.setTaskId(taskId);
        session.setClientToken(request.clientToken());
        session.setDurationMinutes(request.durationMinutes());
        session.setCompletedAt(LocalDateTime.now().withNano(0));

        try {
            sessionMapper.insert(session);
            return PomodoroSessionResponse.from(session);
        } catch (DuplicateKeyException ex) {
            PomodoroSessionRow existing = sessionMapper.findByUserIdAndClientToken(
                    userId, request.clientToken());
            if (existing == null) {
                throw ex;
            }
            return PomodoroSessionResponse.from(existing);
        }
    }
}
