package com.pomodoro;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PomodoroApplication {

    public static void main(String[] args) {
        // 统计以 Asia/Shanghai 为准，避免跨午夜归属错误。
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(PomodoroApplication.class, args);
    }
}
