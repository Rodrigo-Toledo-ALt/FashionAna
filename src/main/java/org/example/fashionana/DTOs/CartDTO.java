package org.example.fashionana.DTOs;

import jakarta.persistence.Entity;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long customerId;
    private int totalItems;
    private BigDecimal totalAmount;
    private List<CartItemDTO> items;


}
