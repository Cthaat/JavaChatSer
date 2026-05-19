package com.example.javachat.stats.dto;

public record StatsOverviewResponse(
        String scope,
        long registeredUserCount,
        long onlineUserCount,
        long todayPrivateMessageCount,
        long todayPublicMessageCount,
        long totalPrivateMessageCount,
        long totalPublicMessageCount,
        long pendingFriendRequestCount,
        long acceptedFriendCount
) {
}
