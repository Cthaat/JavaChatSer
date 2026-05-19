package com.example.javachat.friend;

import com.example.javachat.common.ApiResponse;
import com.example.javachat.friend.dto.FriendRequestCreateRequest;
import com.example.javachat.friend.dto.FriendRequestResponse;
import com.example.javachat.friend.dto.FriendResponse;
import com.example.javachat.security.LoginUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public ApiResponse<List<FriendResponse>> listFriends(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(friendService.listFriends(loginUser.id()));
    }

    @PostMapping("/requests")
    public ApiResponse<FriendRequestResponse> sendRequest(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody FriendRequestCreateRequest request
    ) {
        return ApiResponse.success(friendService.sendRequest(loginUser.id(), request));
    }

    @GetMapping("/requests")
    public ApiResponse<List<FriendRequestResponse>> listReceivedRequests(
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return ApiResponse.success(friendService.listReceivedRequests(loginUser.id()));
    }

    @PostMapping("/requests/{requestId}/accept")
    public ApiResponse<FriendRequestResponse> acceptRequest(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable @Positive(message = "好友申请 ID 必须为正数") Long requestId
    ) {
        return ApiResponse.success(friendService.acceptRequest(loginUser.id(), requestId));
    }

    @PostMapping("/requests/{requestId}/reject")
    public ApiResponse<FriendRequestResponse> rejectRequest(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable @Positive(message = "好友申请 ID 必须为正数") Long requestId
    ) {
        return ApiResponse.success(friendService.rejectRequest(loginUser.id(), requestId));
    }

    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> deleteFriend(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable @Positive(message = "好友 ID 必须为正数") Long friendId
    ) {
        friendService.deleteFriend(loginUser.id(), friendId);
        return ApiResponse.success();
    }
}
