package org.example.fashionana.Repositorios.Clientes;

import org.example.fashionana.Modelos.Clientes.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}
