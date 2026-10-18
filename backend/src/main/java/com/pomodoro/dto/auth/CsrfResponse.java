package com.pomodoro.dto.auth;

public record CsrfResponse(String token, String headerName) {
}
