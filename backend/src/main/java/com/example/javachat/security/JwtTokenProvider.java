package com.example.javachat.security;

import com.example.javachat.common.BusinessException;
import com.example.javachat.common.ErrorCode;
import com.example.javachat.user.User;
import com.example.javachat.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public JwtTokenProvider(JwtProperties properties, UserRepository userRepository) {
        this(properties, userRepository, Clock.systemDefaultZone());
    }

    JwtTokenProvider(JwtProperties properties, UserRepository userRepository, Clock clock) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public String createToken(User user) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusMillis(properties.expiration());
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    public LoginUser loadUser(String token) {
        Long userId = parseUserId(token);
        User user = userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return LoginUser.from(user);
    }

    private Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private SecretKey signingKey() {
        byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
