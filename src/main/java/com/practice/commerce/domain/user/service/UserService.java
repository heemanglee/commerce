package com.practice.commerce.domain.user.service;

import com.practice.commerce.common.exception.DuplicateUserEmailException;
import com.practice.commerce.common.exception.DuplicateUserNameException;
import com.practice.commerce.common.exception.NotFoundUserException;
import com.practice.commerce.common.utils.JwtIssuer;
import com.practice.commerce.domain.user.controller.response.CreateUserResponse;
import com.practice.commerce.domain.user.controller.response.LoginUserResponse;
import com.practice.commerce.domain.user.entity.User;
import com.practice.commerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final RedisTemplate<String, String> redisTemplate;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public CreateUserResponse createUser(String name, String email, String password) {
        validateUserNameAndEmail(name, email);

        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .build();
        User savedUser = userRepository.save(user);
        return new CreateUserResponse(savedUser.getId());
    }

    @Transactional(readOnly = true)
    public LoginUserResponse loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundUserException("사용자 조회에 실패했습니다. email = " + email));
        validateAuthentication(password, user);

        String accessToken = jwtIssuer.generateAccessToken(user);
        String refreshToken = jwtIssuer.generateRefreshToken(user);

        refreshTokenService.save(user.getEmail(), refreshToken);

        return LoginUserResponse.from("Bearer", accessToken, refreshToken);
    }

    private void validateAuthentication(String password, User user) {
        boolean isMatchPassword = passwordEncoder.matches(password, user.getPassword());
        if (!isMatchPassword) {
            throw new BadCredentialsException("사용자 패스워드가 일치하지 않습니다.");
        }
    }

    private void validateUserNameAndEmail(String name, String email) {
        validateDuplicateUserName(name);
        validateDuplicateUserEmail(email);
    }

    private void validateDuplicateUserName(String name) throws DuplicateUserNameException {
        boolean existName = userRepository.existsByName(name);
        if (existName) {
            throw new DuplicateUserNameException("이미 사용 중인 이름입니다. name = " + name);
        }
    }

    private void validateDuplicateUserEmail(String email) {
        boolean existUser = userRepository.existsByEmail(email);
        if (existUser) {
            throw new DuplicateUserEmailException("이미 사용 중인 이메일입니다. email = " + email);
        }
    }
}
