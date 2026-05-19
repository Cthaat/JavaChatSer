package com.example.javachat.friend;

import com.example.javachat.chat.PrivateMessageRepository;
import com.example.javachat.common.BusinessException;
import com.example.javachat.common.ErrorCode;
import com.example.javachat.friend.dto.FriendRequestCreateRequest;
import com.example.javachat.friend.dto.FriendRequestResponse;
import com.example.javachat.friend.dto.FriendResponse;
import com.example.javachat.friend.dto.FriendUserResponse;
import com.example.javachat.user.User;
import com.example.javachat.user.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final FriendCacheService friendCacheService;

    public FriendService(
            FriendRepository friendRepository,
            UserRepository userRepository,
            PrivateMessageRepository privateMessageRepository,
            FriendCacheService friendCacheService
    ) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.privateMessageRepository = privateMessageRepository;
        this.friendCacheService = friendCacheService;
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> listFriends(Long userId) {
        List<FriendUserResponse> friendUsers = friendCacheService.getFriendUsers(userId)
                .orElseGet(() -> {
                    List<FriendRelation> relations = friendRepository.findByUserIdAndStatus(
                            userId,
                            FriendStatus.ACCEPTED
                    );
                    List<Long> friendIds = relations.stream()
                            .map(FriendRelation::getFriendId)
                            .toList();
                    List<FriendUserResponse> friends = buildFriendUsers(friendIds);
                    friendCacheService.putFriendUsers(userId, friends);
                    return friends;
                });
        return friendUsers.stream()
                .map(friend -> new FriendResponse(
                        friend,
                        friendCacheService.isOnline(friend.id()),
                        privateMessageRepository.countByReceiverIdAndSenderIdAndReadAtIsNull(userId, friend.id())
                ))
                .toList();
    }

    @Transactional
    public FriendRequestResponse sendRequest(Long requesterId, FriendRequestCreateRequest request) {
        Long friendId = request.friendId();
        if (requesterId.equals(friendId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能添加自己为好友");
        }

        User requester = findEnabledUser(requesterId);
        User targetUser = findEnabledUser(friendId);

        FriendRelation existingForward = friendRepository.findByUserIdAndFriendId(requesterId, friendId)
                .orElse(null);
        FriendRelation existingReverse = friendRepository.findByUserIdAndFriendId(friendId, requesterId)
                .orElse(null);
        if (isActiveRelation(existingForward) || isActiveRelation(existingReverse)) {
            throw new BusinessException(ErrorCode.CONFLICT, "好友关系或申请已存在");
        }

        FriendRelation relation = existingForward == null
                ? new FriendRelation(requester.getId(), targetUser.getId(), FriendStatus.PENDING)
                : existingForward;
        relation.setStatus(FriendStatus.PENDING);
        FriendRelation savedRelation = friendRepository.save(relation);
        return FriendRequestResponse.from(savedRelation, FriendUserResponse.from(requester));
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> listReceivedRequests(Long userId) {
        return friendRepository.findByFriendIdAndStatus(userId, FriendStatus.PENDING)
                .stream()
                .sorted(Comparator.comparing(FriendRelation::getCreatedAt).reversed())
                .map(relation -> FriendRequestResponse.from(
                        relation,
                        FriendUserResponse.from(findEnabledUser(relation.getUserId()))
                ))
                .toList();
    }

    @Transactional
    public FriendRequestResponse acceptRequest(Long userId, Long requestId) {
        FriendRelation request = findPendingRequestForUser(userId, requestId);
        request.setStatus(FriendStatus.ACCEPTED);
        FriendRelation savedRequest = friendRepository.save(request);

        FriendRelation reverseRelation = friendRepository.findByUserIdAndFriendId(userId, request.getUserId())
                .orElseGet(() -> new FriendRelation(userId, request.getUserId(), FriendStatus.ACCEPTED));
        reverseRelation.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(reverseRelation);

        evictFriendCaches(userId, request.getUserId());
        return FriendRequestResponse.from(savedRequest, FriendUserResponse.from(findEnabledUser(request.getUserId())));
    }

    @Transactional
    public FriendRequestResponse rejectRequest(Long userId, Long requestId) {
        FriendRelation request = findPendingRequestForUser(userId, requestId);
        request.setStatus(FriendStatus.REJECTED);
        FriendRelation savedRequest = friendRepository.save(request);
        return FriendRequestResponse.from(savedRequest, FriendUserResponse.from(findEnabledUser(request.getUserId())));
    }

    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        FriendRelation relation = friendRepository.findByUserIdAndFriendId(userId, friendId)
                .filter(item -> item.getStatus() == FriendStatus.ACCEPTED)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "好友关系不存在"));
        FriendRelation reverseRelation = friendRepository.findByUserIdAndFriendId(friendId, userId)
                .filter(item -> item.getStatus() == FriendStatus.ACCEPTED)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "好友关系不存在"));

        relation.setStatus(FriendStatus.DELETED);
        reverseRelation.setStatus(FriendStatus.DELETED);
        friendRepository.save(relation);
        friendRepository.save(reverseRelation);
        evictFriendCaches(userId, friendId);
    }

    private List<FriendUserResponse> buildFriendUsers(List<Long> friendIds) {
        if (friendIds.isEmpty()) {
            return List.of();
        }
        Map<Long, User> usersById = userRepository.findAllById(friendIds)
                .stream()
                .filter(User::isEnabled)
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return friendIds.stream()
                .map(usersById::get)
                .filter(user -> user != null)
                .sorted(Comparator.comparing(User::getUsername))
                .map(FriendUserResponse::from)
                .toList();
    }

    private FriendRelation findPendingRequestForUser(Long userId, Long requestId) {
        return friendRepository.findById(requestId)
                .filter(request -> request.getFriendId().equals(userId))
                .filter(request -> request.getStatus() == FriendStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "好友申请不存在"));
    }

    private User findEnabledUser(Long userId) {
        return userRepository.findByIdAndEnabledTrue(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
    }

    private static boolean isActiveRelation(FriendRelation relation) {
        return relation != null
                && (relation.getStatus() == FriendStatus.PENDING || relation.getStatus() == FriendStatus.ACCEPTED);
    }

    private void evictFriendCaches(Long userId, Long friendId) {
        friendCacheService.evictFriendList(userId);
        friendCacheService.evictFriendList(friendId);
    }
}
