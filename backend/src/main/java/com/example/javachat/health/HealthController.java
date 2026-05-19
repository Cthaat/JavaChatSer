package com.example.javachat.health;

import com.example.javachat.common.ApiResponse;
import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(new HealthResponse(
                "UP",
                "javachat-backend",
                OffsetDateTime.now()
        ));
    }

    public record HealthResponse(String status, String application, OffsetDateTime timestamp) {
    }
}
