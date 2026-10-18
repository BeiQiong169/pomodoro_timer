package com.pomodoro.dto.auth;

import com.pomodoro.validation.MaxBytes;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        @MaxBytes(value = 72, message = "密码不能超过 72 字节")
        String password) {
}
