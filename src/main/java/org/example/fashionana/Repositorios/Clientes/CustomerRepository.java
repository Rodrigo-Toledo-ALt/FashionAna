package org.example.fashionana.Repositorios.Clientes;

import org.example.fashionana.Modelos.Clientes.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
