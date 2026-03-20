package com.ecommerce.project.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
