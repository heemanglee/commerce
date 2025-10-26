package com.practice.commerce.domain.user.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUserResponse {

    private String type;
    private String accessToken;
    private String refreshToken;

    public static LoginUserResponse from(String type, String accessToken, String refreshToken) {
        return new LoginUserResponse(type, accessToken, refreshToken);
    }
}
