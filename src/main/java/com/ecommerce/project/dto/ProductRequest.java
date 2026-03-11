package com.ecommerce.project.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Min(value = 0)
    private Integer stockQuantity;

    @NotEmpty
    private Set<Integer> categoryIds;
}
