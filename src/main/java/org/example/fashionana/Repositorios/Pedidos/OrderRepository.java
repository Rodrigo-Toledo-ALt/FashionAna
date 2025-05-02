package org.example.fashionana.Repositorios.Pedidos;

import org.example.fashionana.Modelos.Pedidos.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByShippingAddressId(Long addressId);
    List<Order> findByBillingAddressId(Long addressId);

    @Query("SELECT o FROM Order o JOIN o.customer c WHERE CONCAT(c.firstName, ' ', c.lastName) LIKE %:name%")
    List<Order> findByCustomerNameContaining(@Param("name") String name);

    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
