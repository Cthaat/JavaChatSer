package com.example.javachat.chat;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    @Query("""
            select message
            from PrivateMessage message
            where (message.senderId = :userId and message.receiverId = :friendId)
               or (message.senderId = :friendId and message.receiverId = :userId)
            order by message.createdAt asc
            """)
    Page<PrivateMessage> findConversation(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId,
            Pageable pageable
    );

    long countByReceiverIdAndSenderIdAndReadAtIsNull(Long receiverId, Long senderId);

    @Modifying
    @Query("""
            update PrivateMessage message
            set message.readAt = :readAt
            where message.receiverId = :receiverId
              and message.senderId = :senderId
              and message.readAt is null
            """)
    int markMessagesAsRead(
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId,
            @Param("readAt") LocalDateTime readAt
    );
}
