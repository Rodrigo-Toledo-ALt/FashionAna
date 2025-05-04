package org.example.fashionana.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CartItemDTO {

    private Long variantId;
    private String productName;
    private String size;
    private String color;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

}
