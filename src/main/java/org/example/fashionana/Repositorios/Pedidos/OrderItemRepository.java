package org.example.fashionana.Repositorios.Pedidos;

import org.example.fashionana.Modelos.Pedidos.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
