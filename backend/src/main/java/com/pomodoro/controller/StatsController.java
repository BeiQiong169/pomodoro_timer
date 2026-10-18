package com.pomodoro.controller;

import com.pomodoro.dto.stats.StatsSummaryResponse;
import com.pomodoro.security.AppUserPrincipal;
import com.pomodoro.service.StatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public StatsSummaryResponse summary(@AuthenticationPrincipal AppUserPrincipal principal) {
        return statsService.summary(principal.getId());
    }
}
