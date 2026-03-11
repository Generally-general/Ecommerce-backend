package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class CategoryRequest {
    @NotBlank
    private String name;
}
