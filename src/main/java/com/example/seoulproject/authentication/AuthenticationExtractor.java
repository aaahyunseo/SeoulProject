package com.example.seoulproject.authentication;


import com.example.seoulproject.exception.UnauthorizedException;
import com.example.seoulproject.exception.errorcode.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class AuthenticationExtractor {
    public static final String TOKEN_COOKIE_NAME = "AccessToken";

    public static String extract(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return JwtEncoder.decode(String.valueOf(cookie.getValue()));
                }
            }
        }
        throw new UnauthorizedException(ErrorCode.COOKIE_NOT_FOUND);
    }
}
