package com.example.seoulproject.controller;

import com.example.seoulproject.authentication.JwtEncoder;
import com.example.seoulproject.dto.request.auth.LoginDto;
import com.example.seoulproject.dto.request.auth.SignupDto;
import com.example.seoulproject.dto.response.ResponseDto;
import com.example.seoulproject.dto.response.TokenResponseDto;
import com.example.seoulproject.service.AuthService;
import com.example.seoulproject.service.CookieService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseDto<Void>> signup(@RequestBody @Valid SignupDto signupDto, HttpServletResponse response) {
        TokenResponseDto tokenResponseDto = authService.signup(signupDto);
        cookieService.setCookie(response, JwtEncoder.encode(tokenResponseDto.getAccessToken()));
        return new ResponseEntity<>(ResponseDto.res(HttpStatus.CREATED, "회원가입 완료"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<Void>> login(@RequestBody @Valid LoginDto loginDto, HttpServletResponse response) {
        TokenResponseDto tokenResponseDto = authService.login(loginDto);
        cookieService.setCookie(response, JwtEncoder.encode(tokenResponseDto.getAccessToken()));
        return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "로그인 완료"), HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(final HttpServletResponse response) {
        clearCookies(response);
        return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "로그아웃 완료"), HttpStatus.OK);
    }

    private void clearCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("AccessToken", null)
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());
    }
}
