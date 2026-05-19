package com.example.javachat.user;

import com.example.javachat.common.BusinessException;
import com.example.javachat.common.ErrorCode;
import com.example.javachat.security.JwtTokenProvider;
import com.example.javachat.security.LoginUser;
import com.example.javachat.user.dto.AuthResponse;
import com.example.javachat.user.dto.LoginRequest;
import com.example.javachat.user.dto.RegisterRequest;
import com.example.javachat.user.dto.UserProfileResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private static final String DEFAULT_AVATAR_URL = "/default-avatar.png";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        String nickname = StringUtils.hasText(request.nickname()) ? request.nickname().trim() : username;
        String avatarUrl = StringUtils.hasText(request.avatarUrl()) ? request.avatarUrl().trim() : DEFAULT_AVATAR_URL;
        User user = new User(username, passwordEncoder.encode(request.password()), nickname);
        user.setAvatarUrl(avatarUrl);
        User savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .filter(User::isEnabled)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse currentUser(LoginUser loginUser) {
        User user = userRepository.findById(loginUser.id())
                .filter(User::isEnabled)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return UserProfileResponse.from(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(jwtTokenProvider.createToken(user), UserProfileResponse.from(user));
    }
}
