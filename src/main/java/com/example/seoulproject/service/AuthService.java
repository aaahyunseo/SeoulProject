package com.example.seoulproject.service;

import com.example.seoulproject.authentication.AccessTokenProvider;
import com.example.seoulproject.authentication.PasswordHashEncryption;
import com.example.seoulproject.dto.request.auth.LoginDto;
import com.example.seoulproject.dto.request.auth.SignupDto;
import com.example.seoulproject.dto.response.TokenResponseDto;
import com.example.seoulproject.entity.User;
import com.example.seoulproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordHashEncryption passwordHashEncryption;
    private final UserRepository userRepository;
    private final AccessTokenProvider accessTokenProvider;
    private final UserService userService;

    public TokenResponseDto signup(SignupDto signupDto) {

        userService.validateIsDuplicatedName(signupDto.getName());

        userService.validateIsDuplicatedEmail(signupDto.getEmail());

        String plainPassword = signupDto.getPassword();
        String hashedPassword = passwordHashEncryption.encrypt(plainPassword);

        User newUser = User.builder()
                .email(signupDto.getEmail())
                .password(hashedPassword)
                .name(signupDto.getName())
                .build();
        userRepository.save(newUser);

        return createToken(newUser);
    }

    public TokenResponseDto login(LoginDto loginDto) {
        User user = userService.findExistingUserByEmail(loginDto.getEmail());

        userService.validateIsPasswordMatches(loginDto.getPassword(), user.getPassword());

        return createToken(user);
    }

    private TokenResponseDto createToken(User user) {
        String payload = String.valueOf(user.getId());
        String accessToken = accessTokenProvider.createToken(payload);

        return new TokenResponseDto(accessToken);
    }
}
