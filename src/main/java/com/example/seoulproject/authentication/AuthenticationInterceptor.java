package com.example.seoulproject.authentication;

import com.example.seoulproject.entity.User;
import com.example.seoulproject.exception.NotFoundException;
import com.example.seoulproject.exception.errorcode.ErrorCode;
import com.example.seoulproject.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;
    private final AccessTokenProvider accessTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String accessToken = AuthenticationExtractor.extract(request);
        if (accessToken == null || accessToken.isBlank()) {
            return true; // 로그인 안 된 상태로 통과시킴
        }

        try {
            UUID userId = UUID.fromString(accessTokenProvider.getPayload(accessToken));
            User user = findExistingUser(userId);
            authenticationContext.setPrincipal(user);
        } catch (Exception e) {
            // 토큰은 있었지만 유효하지 않은 경우
            log.warn("Invalid token during authentication: {}", e.getMessage());
        }
        return true;
    }

    private User findExistingUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
