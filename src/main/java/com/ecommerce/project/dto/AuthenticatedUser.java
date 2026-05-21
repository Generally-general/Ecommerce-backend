package com.ecommerce.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {

    private Integer id;
    private String email;
    private String role;
}
