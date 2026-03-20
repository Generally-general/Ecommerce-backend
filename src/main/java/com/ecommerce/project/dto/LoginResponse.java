package com.ecommerce.project.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse {
    private UserResponse user;
    private String token;
    private String refreshToken;

    public LoginResponse(UserResponse user, String token, String refreshToken) {
        this.user = user;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
