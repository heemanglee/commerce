package com.practice.commerce.domain.user.controller;

import com.practice.commerce.domain.user.controller.request.CreateUserRequest;
import com.practice.commerce.domain.user.controller.response.CreateUserResponse;
import com.practice.commerce.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<CreateUserResponse> signUp(
            @Valid @RequestBody CreateUserRequest request
    ) {
        CreateUserResponse response = userService.createUser(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
