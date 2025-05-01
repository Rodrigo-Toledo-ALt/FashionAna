package org.example.fashionana.Repositorios.Pedidos;

import org.example.fashionana.Modelos.Pedidos.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByShippingAddressId(Long addressId);
    List<Order> findByBillingAddressId(Long addressId);
}
