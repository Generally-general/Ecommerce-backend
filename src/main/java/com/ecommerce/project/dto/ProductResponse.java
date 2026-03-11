package com.ecommerce.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private LocalDateTime createdAt;
    private Set<CategoryResponse> categories;
}
