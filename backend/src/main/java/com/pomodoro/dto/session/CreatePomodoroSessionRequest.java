package com.pomodoro.dto.session;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePomodoroSessionRequest(
        @Positive(message = "任务 id 必须为正数")
        Long taskId,

        @NotNull(message = "时长不能为空")
        @Positive(message = "时长必须大于 0")
        @Max(value = 1440, message = "时长不能超过 1440 分钟")
        Integer durationMinutes,

        @NotBlank(message = "clientToken 不能为空")
        @Size(max = 36, message = "clientToken 不能超过 36 个字符")
        String clientToken) {
}
