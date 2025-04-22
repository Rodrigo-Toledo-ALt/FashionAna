package org.example.fashionana.Servicios.Pedidos;

import org.example.fashionana.Modelos.OrderStatus;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Modelos.Pedidos.OrderStatusHistory;
import org.example.fashionana.Repositorios.Pedidos.OrderRepository;
import org.example.fashionana.Repositorios.Pedidos.OrderStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        // This would require a custom query method in the repository
        // For now, we'll implement a simple filter
        return orderRepository.findAll().stream()
                .filter(order -> order.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        // This would require a custom query method in the repository
        // For now, we'll implement a simple filter
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(newStatus);
            
            // Create status history entry
            OrderStatusHistory statusHistory = new OrderStatusHistory();
            statusHistory.setOrder(order);
            statusHistory.setOldStatus(oldStatus);
            statusHistory.setNewStatus(newStatus);
            statusHistory.setChangedAt(LocalDateTime.now());
            
            // Save both entities
            orderStatusHistoryRepository.save(statusHistory);
            return orderRepository.save(order);
        }
        return null;
    }
    
    @Override
    public List<Order> findRecentOrdersByCustomerId(Long customerId, int limit) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getCustomer().getId().equals(customerId))
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findRecentOrders(int limit) {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public int countAll() {
        return orderRepository.findAll().size();
    }
}