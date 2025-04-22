package org.example.fashionana.Servicios.Clientes;

import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Repositorios.Clientes.CustomerRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public Customer save(Customer customer) {
        // Hashear la contraseña manualmente
        String hashedPassword = BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt());
        customer.setPassword(hashedPassword);
        return customerRepository.save(customer);
    }

    // Para verificar después:
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        // This would require a custom query method in the repository
        // For now, we'll implement a simple search
        return customerRepository.findAll().stream()
                .filter(customer -> customer.getEmail().equals(email))
                .findFirst();
    }
    
    @Override
    public int countAll() {
        return customerRepository.findAll().size();
    }
}