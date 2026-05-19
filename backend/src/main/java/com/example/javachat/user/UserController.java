package com.example.javachat.user;

import com.example.javachat.common.ApiResponse;
import com.example.javachat.common.PageResponse;
import com.example.javachat.security.LoginUser;
import com.example.javachat.user.dto.UserSearchResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<UserSearchResponse>> searchUsers(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam
            @NotBlank(message = "搜索关键词不能为空")
            @Size(max = 50, message = "搜索关键词最多 50 个字符")
            String keyword,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "页码不能小于 0")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量不能小于 1")
            @Max(value = 50, message = "每页数量不能超过 50")
            int size
    ) {
        return ApiResponse.success(userService.searchUsers(
                loginUser.id(),
                keyword,
                PageRequest.of(page, size)
        ));
    }
}
