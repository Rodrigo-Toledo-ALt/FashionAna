package org.example.fashionana.Modelos.Clientes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fashionana.Modelos.Productos.ProductVariant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {
    private Customer customer;
    private List<CartItem> items = new ArrayList<>();
    
    public void addItem(ProductVariant variant, Integer quantity) {
        // Check if the variant is already in the cart
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.setVariant(variant);
            newItem.setQuantity(quantity);
            newItem.setPrice(variant.getPrice());
            items.add(newItem);
        }
    }
    
    public void updateItemQuantity(Long variantId, Integer quantity) {
        items.stream()
                .filter(item -> item.getVariant().getId().equals(variantId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        removeItem(variantId);
                    } else {
                        item.setQuantity(quantity);
                    }
                });
    }
    
    public void removeItem(Long variantId) {
        items.removeIf(item -> item.getVariant().getId().equals(variantId));
    }
    
    public void clear() {
        items.clear();
    }
    
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}