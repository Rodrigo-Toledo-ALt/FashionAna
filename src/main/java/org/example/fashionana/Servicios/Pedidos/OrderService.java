package org.example.fashionana.Servicios.Pedidos;

import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Modelos.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> findAll();
    Optional<Order> findById(Long id);
    Order save(Order order);
    void deleteById(Long id);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);
    Order updateOrderStatus(Long id, OrderStatus newStatus);
}