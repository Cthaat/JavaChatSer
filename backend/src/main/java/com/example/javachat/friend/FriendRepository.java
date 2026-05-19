package com.example.javachat.friend;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<FriendRelation, Long> {

    Optional<FriendRelation> findByUserIdAndFriendId(Long userId, Long friendId);

    boolean existsByUserIdAndFriendIdAndStatus(Long userId, Long friendId, FriendStatus status);

    List<FriendRelation> findByUserIdAndStatus(Long userId, FriendStatus status);

    List<FriendRelation> findByFriendIdAndStatus(Long friendId, FriendStatus status);

    List<FriendRelation> findByUserIdAndFriendIdInAndStatus(
            Long userId,
            Collection<Long> friendIds,
            FriendStatus status
    );
}
