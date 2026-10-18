package com.pomodoro.controller;

import com.pomodoro.dto.session.CreatePomodoroSessionRequest;
import com.pomodoro.dto.session.PomodoroSessionResponse;
import com.pomodoro.security.AppUserPrincipal;
import com.pomodoro.service.PomodoroSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pomodoro-sessions")
public class PomodoroSessionController {

    private final PomodoroSessionService pomodoroSessionService;

    public PomodoroSessionController(PomodoroSessionService pomodoroSessionService) {
        this.pomodoroSessionService = pomodoroSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PomodoroSessionResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @Valid @RequestBody CreatePomodoroSessionRequest request) {
        return pomodoroSessionService.record(principal.getId(), request);
    }
}
