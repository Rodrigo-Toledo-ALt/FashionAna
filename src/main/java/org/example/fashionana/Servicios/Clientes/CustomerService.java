package org.example.fashionana.Servicios.Clientes;

import org.example.fashionana.Modelos.Clientes.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    List<Customer> findAll();
    Optional<Customer> findById(Long id);
    Customer save(Customer customer);
    void deleteById(Long id);
    Optional<Customer> findByEmail(String email);
    boolean checkPassword(String plainPassword, String hashedPassword);
}