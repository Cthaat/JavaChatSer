package com.example.javachat.stats;

import com.example.javachat.chat.PrivateMessageRepository;
import com.example.javachat.chat.PublicMessageRepository;
import com.example.javachat.friend.FriendRepository;
import com.example.javachat.friend.FriendStatus;
import com.example.javachat.security.LoginUser;
import com.example.javachat.stats.dto.StatsOverviewResponse;
import com.example.javachat.user.UserRepository;
import com.example.javachat.user.UserRole;
import com.example.javachat.websocket.WebSocketSessionManager;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final UserRepository userRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final PublicMessageRepository publicMessageRepository;
    private final FriendRepository friendRepository;
    private final WebSocketSessionManager sessionManager;

    public StatsService(
            UserRepository userRepository,
            PrivateMessageRepository privateMessageRepository,
            PublicMessageRepository publicMessageRepository,
            FriendRepository friendRepository,
            WebSocketSessionManager sessionManager
    ) {
        this.userRepository = userRepository;
        this.privateMessageRepository = privateMessageRepository;
        this.publicMessageRepository = publicMessageRepository;
        this.friendRepository = friendRepository;
        this.sessionManager = sessionManager;
    }

    @Transactional(readOnly = true)
    public StatsOverviewResponse overview(LoginUser loginUser) {
        var todayStart = LocalDate.now(ZoneId.systemDefault()).atStartOfDay();
        if (loginUser.role() == UserRole.ADMIN) {
            return new StatsOverviewResponse(
                    "GLOBAL",
                    userRepository.countByEnabledTrue(),
                    sessionManager.onlineUserIds().size(),
                    privateMessageRepository.countByCreatedAtGreaterThanEqual(todayStart),
                    publicMessageRepository.countByCreatedAtGreaterThanEqual(todayStart),
                    privateMessageRepository.count(),
                    publicMessageRepository.count(),
                    friendRepository.countByStatus(FriendStatus.PENDING),
                    friendRepository.countByStatus(FriendStatus.ACCEPTED) / 2
            );
        }

        Long userId = loginUser.id();
        return new StatsOverviewResponse(
                "PERSONAL",
                1,
                sessionManager.hasOpenSession(userId) ? 1 : 0,
                privateMessageRepository.countByParticipantSince(userId, todayStart),
                publicMessageRepository.countBySenderIdAndCreatedAtGreaterThanEqual(userId, todayStart),
                privateMessageRepository.countByParticipant(userId),
                publicMessageRepository.countBySenderId(userId),
                friendRepository.countByFriendIdAndStatus(userId, FriendStatus.PENDING),
                friendRepository.countByUserIdAndStatus(userId, FriendStatus.ACCEPTED)
        );
    }
}
