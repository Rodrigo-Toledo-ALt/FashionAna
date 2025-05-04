package org.example.fashionana.Servicios.Clientes;

import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Clientes.Address;
import org.example.fashionana.Modelos.Clientes.CartItem;
import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Modelos.Clientes.PaymentMethod;
import org.example.fashionana.Modelos.Clientes.ShoppingCart;
import org.example.fashionana.Modelos.DeliveryType;
import org.example.fashionana.Modelos.OrderStatus;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Modelos.Pedidos.OrderItem;
import org.example.fashionana.Modelos.Productos.ProductVariant;
import org.example.fashionana.Repositorios.Clientes.AddressRepository;
import org.example.fashionana.Repositorios.Clientes.CustomerRepository;
import org.example.fashionana.Repositorios.Clientes.PaymentMethodRepository;
import org.example.fashionana.Repositorios.Pedidos.OrderRepository;
import org.example.fashionana.Repositorios.Productos.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@SessionScope
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final Map<Long, ShoppingCart> customerCarts = new HashMap<>();
    
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderRepository orderRepository;
    
    @Autowired
    public ShoppingCartServiceImpl(CustomerRepository customerRepository,
                                 ProductVariantRepository productVariantRepository,
                                 AddressRepository addressRepository,
                                 PaymentMethodRepository paymentMethodRepository,
                                 OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.productVariantRepository = productVariantRepository;
        this.addressRepository = addressRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.orderRepository = orderRepository;
    }
    
    @Override
    public ShoppingCart addToCart(Long customerId, Long variantId, Integer quantity) throws BusinessLogicException {
        if (quantity <= 0) {
            throw new BusinessLogicException("Quantity must be greater than zero");
        }
        
        ShoppingCart cart = getCart(customerId);
        Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
        
        if (!variantOpt.isPresent()) {
            throw new BusinessLogicException("Product variant not found");
        }
        
        ProductVariant variant = variantOpt.get();
        if (variant.getStockQuantity() < quantity) {
            throw new BusinessLogicException("Not enough items in stock");
        }
        
        cart.addItem(variant, quantity);
        customerCarts.put(customerId, cart);
        
        return cart;
    }
    
    @Override
    public ShoppingCart updateCartItem(Long customerId, Long variantId, Integer quantity) {
        ShoppingCart cart = getCart(customerId);
        cart.updateItemQuantity(variantId, quantity);
        customerCarts.put(customerId, cart);
        
        return cart;
    }
    
    @Override
    public ShoppingCart removeFromCart(Long customerId, Long variantId) {
        ShoppingCart cart = getCart(customerId);
        cart.removeItem(variantId);
        customerCarts.put(customerId, cart);
        
        return cart;
    }
    
    @Override
    public ShoppingCart getCart(Long customerId) {
        if (!customerCarts.containsKey(customerId)) {
            Optional<Customer> customer = customerRepository.findById(customerId);
            if (customer.isPresent()) {
                ShoppingCart cart = new ShoppingCart();
                cart.setCustomer(customer.get());
                customerCarts.put(customerId, cart);
            } else {
                throw new IllegalArgumentException("Customer not found");
            }
        }
        
        return customerCarts.get(customerId);
    }
    
    @Override
    public void clearCart(Long customerId) {
        if (customerCarts.containsKey(customerId)) {
            ShoppingCart cart = customerCarts.get(customerId);
            cart.clear();
            customerCarts.put(customerId, cart);
        }
    }
    
    @Override
    @Transactional
    public Order checkout(Long customerId, Long shippingAddressId, Long billingAddressId, 
                         Long paymentMethodId, String deliveryType) throws BusinessLogicException {
        
        ShoppingCart cart = getCart(customerId);
        
        if (cart.getItems().isEmpty()) {
            throw new BusinessLogicException("Shopping cart is empty");
        }
        
        // Validate customer
        Customer customer = cart.getCustomer();
        if (!customer.getId().equals(customerId)) {
            throw new BusinessLogicException("Invalid customer");
        }
        
        // Validate addresses
        Optional<Address> shippingAddressOpt = addressRepository.findById(shippingAddressId);
        Optional<Address> billingAddressOpt = addressRepository.findById(billingAddressId);
        Optional<PaymentMethod> paymentMethodOpt = paymentMethodRepository.findById(paymentMethodId);
        
        if (!shippingAddressOpt.isPresent()) {
            throw new BusinessLogicException("Shipping address not found");
        }
        
        if (!billingAddressOpt.isPresent()) {
            throw new BusinessLogicException("Billing address not found");
        }
        
        if (!paymentMethodOpt.isPresent()) {
            throw new BusinessLogicException("Payment method not found");
        }
        
        Address shippingAddress = shippingAddressOpt.get();
        Address billingAddress = billingAddressOpt.get();
        PaymentMethod paymentMethod = paymentMethodOpt.get();
        
        // Create new order
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setDeliveryType(DeliveryType.valueOf(deliveryType));
        order.setTotalAmount(cart.getTotalAmount());

        order.setOrderItems(new ArrayList<>());

        // Save order to get ID
        order = orderRepository.save(order);
        
        // Create order items
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();
            
            // Check stock availability
            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessLogicException("Not enough stock for " + variant.getProduct().getName() + 
                                              " (" + variant.getSize() + ", " + variant.getColor() + ")");
            }
            
            // Update stock
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);
            
            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPrice());
            orderItem.setSubtotal(cartItem.getSubtotal());
            
            // Add order item to order
            order.getOrderItems().add(orderItem);
        }
        
        // Save order with items
        order = orderRepository.save(order);
        
        // Clear cart
        clearCart(customerId);
        
        return order;
    }
}