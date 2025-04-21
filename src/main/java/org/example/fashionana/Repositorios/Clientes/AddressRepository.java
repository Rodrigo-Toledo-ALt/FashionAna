package org.example.fashionana.Repositorios.Clientes;

import org.example.fashionana.Modelos.Clientes.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
