package com.pomodoro.controller;

import java.util.List;

import com.pomodoro.dto.task.CreateTaskRequest;
import com.pomodoro.dto.task.TaskResponse;
import com.pomodoro.dto.task.UpdateTaskRequest;
import com.pomodoro.security.AppUserPrincipal;
import com.pomodoro.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return taskService.list(principal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                               @Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(principal.getId(), request);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal AppUserPrincipal principal,
                               @PathVariable Long id,
                               @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        taskService.delete(principal.getId(), id);
    }
}
