package com.example.javachat.friend.dto;

import com.example.javachat.friend.FriendRelation;
import java.time.LocalDateTime;

public record FriendRequestResponse(
        Long requestId,
        FriendUserResponse requester,
        String status,
        LocalDateTime createdAt
) {

    public static FriendRequestResponse from(FriendRelation relation, FriendUserResponse requester) {
        return new FriendRequestResponse(
                relation.getId(),
                requester,
                relation.getStatus().name(),
                relation.getCreatedAt()
        );
    }
}
