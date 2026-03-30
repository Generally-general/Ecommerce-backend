package com.ecommerce.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Integer productId;
    private String productName;
    private Integer requestedQuantity;
    private Integer fulfilledQuantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
