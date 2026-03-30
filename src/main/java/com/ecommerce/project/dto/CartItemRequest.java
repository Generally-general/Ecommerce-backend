package com.ecommerce.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "Product ID is required")
    private Integer productId;

    @Min(value = 1, message = "Quantity must be atleast 1")
    private Integer quantity;
}
