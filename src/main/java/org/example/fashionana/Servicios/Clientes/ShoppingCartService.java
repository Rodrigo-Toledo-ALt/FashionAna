package org.example.fashionana.Servicios.Clientes;

import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Clientes.ShoppingCart;
import org.example.fashionana.Modelos.OrderStatus;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Modelos.Pedidos.OrderItem;

public interface ShoppingCartService {
    /**
     * Adds a product variant to the shopping cart
     * @param customerId The ID of the customer
     * @param variantId The ID of the product variant
     * @param quantity The quantity to add
     * @return The updated shopping cart
     */
    ShoppingCart addToCart(Long customerId, Long variantId, Integer quantity) throws BusinessLogicException;
    
    /**
     * Updates the quantity of a product variant in the shopping cart
     * @param customerId The ID of the customer
     * @param variantId The ID of the product variant
     * @param quantity The new quantity
     * @return The updated shopping cart
     */
    ShoppingCart updateCartItem(Long customerId, Long variantId, Integer quantity);
    
    /**
     * Removes a product variant from the shopping cart
     * @param customerId The ID of the customer
     * @param variantId The ID of the product variant
     * @return The updated shopping cart
     */
    ShoppingCart removeFromCart(Long customerId, Long variantId);
    
    /**
     * Gets the shopping cart for a customer
     * @param customerId The ID of the customer
     * @return The shopping cart
     */
    ShoppingCart getCart(Long customerId);
    
    /**
     * Clears the shopping cart for a customer
     * @param customerId The ID of the customer
     */
    void clearCart(Long customerId);
    
    /**
     * Creates an order from the shopping cart
     * @param customerId The ID of the customer
     * @param shippingAddressId The ID of the shipping address
     * @param billingAddressId The ID of the billing address
     * @param paymentMethodId The ID of the payment method
     * @param deliveryType The delivery type
     * @return The created order
     */
    Order checkout(Long customerId, Long shippingAddressId, Long billingAddressId, 
                  Long paymentMethodId, String deliveryType) throws BusinessLogicException;
}