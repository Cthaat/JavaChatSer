package com.example.javachat.stats;

import com.example.javachat.common.ApiResponse;
import com.example.javachat.security.LoginUser;
import com.example.javachat.stats.dto.StatsOverviewResponse;
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

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewResponse> overview(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(statsService.overview(loginUser));
    }
}
