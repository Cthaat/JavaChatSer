package com.example.javachat.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 20, message = "用户名长度必须为 4-20 位")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度必须为 6-32 位")
        String password,

        @Size(max = 50, message = "昵称最多 50 个字符")
        String nickname,

        @Size(max = 255, message = "头像地址最多 255 个字符")
        String avatarUrl
) {
}
