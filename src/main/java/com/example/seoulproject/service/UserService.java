package com.example.seoulproject.service;

import com.example.seoulproject.authentication.PasswordHashEncryption;
import com.example.seoulproject.dto.request.user.DeleteUserDto;
import com.example.seoulproject.dto.response.user.UserResponseDto;
import com.example.seoulproject.entity.User;
import com.example.seoulproject.exception.ConflictException;
import com.example.seoulproject.exception.NotFoundException;
import com.example.seoulproject.exception.UnauthorizedException;
import com.example.seoulproject.exception.errorcode.ErrorCode;
import com.example.seoulproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHashEncryption passwordHashEncryption;

    public UserResponseDto getUserInfo(User user) {
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .name(user.getName())
                .build();
        return userResponseDto;
    }

    public void deleteUser(User user, DeleteUserDto deleteUserDto) {
        validateIsPasswordMatches(deleteUserDto.getPassword(), user.getPassword());
        userRepository.delete(user);
    }

    public void validateIsPasswordMatches(String requestedPassword, String userPassword) {
        if (!passwordHashEncryption.matches(requestedPassword, userPassword)) {
            throw new UnauthorizedException(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }
    }

    public void validateIsDuplicatedEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    public void validateIsDuplicatedName(String name) {
        if (userRepository.existsByName(name)) {
            throw new ConflictException(ErrorCode.DUPLICATED_NAME);
        }
    }

    public User findExistingUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(ErrorCode.INVALID_EMAIL_OR_PASSWORD));
    }
}
