package com.practice.commerce.domain.user.service;

import com.practice.commerce.common.exception.DuplicateUserEmailException;
import com.practice.commerce.domain.user.controller.response.CreateUserResponse;
import com.practice.commerce.domain.user.entity.User;
import com.practice.commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public CreateUserResponse createUser(String email, String password) {
        validateDuplicateUserEmail(email);

        User user = User.builder()
                .email(email)
                .password(password)
                .build();
        User savedUser = userRepository.save(user);
        return new CreateUserResponse(savedUser.getId());
    }

    private void validateDuplicateUserEmail(String email) {
        boolean existUser = userRepository.existsByEmail(email);
        if (existUser) {
            throw new DuplicateUserEmailException("이미 사용 중인 이메일입니다. email = " + email);
        }
    }
}
