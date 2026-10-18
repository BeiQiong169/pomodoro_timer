package com.pomodoro.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "任务标题不能为空")
        @Size(max = 200, message = "任务标题不能超过 200 个字符")
        String title) {
}
