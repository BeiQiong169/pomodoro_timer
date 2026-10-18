package com.pomodoro.dto.task;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 200, message = "任务标题不能超过 200 个字符")
        @Pattern(regexp = "(?s).*\\S.*", message = "任务标题不能为空")
        String title,

        Boolean completed) {
}
