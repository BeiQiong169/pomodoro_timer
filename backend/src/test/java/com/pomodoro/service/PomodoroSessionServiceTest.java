package com.pomodoro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import com.pomodoro.dto.session.CreatePomodoroSessionRequest;
import com.pomodoro.dto.session.PomodoroSessionResponse;
import com.pomodoro.mapper.PomodoroSessionMapper;
import com.pomodoro.mapper.TaskMapper;
import com.pomodoro.model.PomodoroSessionRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class PomodoroSessionServiceTest {

    @Mock
    private PomodoroSessionMapper sessionMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private PomodoroSessionService pomodoroSessionService;

    @Test
    void taskNotOwnedByUserIsSavedAsNull() {
        when(taskMapper.findByIdAndUserId(99L, 1L)).thenReturn(null);
        when(sessionMapper.insert(any(PomodoroSessionRow.class))).thenAnswer(invocation -> {
            PomodoroSessionRow session = invocation.getArgument(0);
            session.setId(5L);
            return 1;
        });

        PomodoroSessionResponse response = pomodoroSessionService.record(
                1L, new CreatePomodoroSessionRequest(99L, 25, "token-1"));

        ArgumentCaptor<PomodoroSessionRow> captor = ArgumentCaptor.forClass(PomodoroSessionRow.class);
        verify(sessionMapper).insert(captor.capture());
        assertThat(captor.getValue().getTaskId()).isNull();
        assertThat(response.taskId()).isNull();
    }

    @Test
    void duplicateTokenReturnsExistingSession() {
        when(sessionMapper.insert(any(PomodoroSessionRow.class)))
                .thenThrow(new DuplicateKeyException("duplicate token"));
        PomodoroSessionRow existing = new PomodoroSessionRow();
        existing.setId(7L);
        existing.setDurationMinutes(25);
        existing.setCompletedAt(LocalDateTime.now().minusMinutes(5));
        when(sessionMapper.findByUserIdAndClientToken(1L, "token-1")).thenReturn(existing);

        PomodoroSessionResponse response = pomodoroSessionService.record(
                1L, new CreatePomodoroSessionRequest(null, 25, "token-1"));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.completedAt()).isEqualTo(existing.getCompletedAt());
    }
}
