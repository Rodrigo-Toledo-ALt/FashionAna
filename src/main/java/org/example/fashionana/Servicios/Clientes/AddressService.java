package org.example.fashionana.Servicios.Clientes;

import org.example.fashionana.Modelos.Clientes.Address;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<Address> findAll();
    Optional<Address> findById(Long id);
    Address save(Address address);
    void deleteById(Long id);
    List<Address> findByCustomerId(Long customerId);
    void unsetDefaultForCustomer(Long customerId);
    void unsetBillingForCustomer(Long customerId);
}