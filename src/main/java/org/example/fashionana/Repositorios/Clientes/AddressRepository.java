package org.example.fashionana.Repositorios.Clientes;

import org.example.fashionana.Modelos.Clientes.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long customerId);



    /**
     * Desmarca todas las direcciones como predeterminadas para un cliente específico.
     *
     * @param customerId ID del cliente
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void unsetDefaultForCustomer(@Param("customerId") Long customerId);

    /**
     * Desmarca todas las direcciones como dirección de facturación para un cliente específico.
     * Nota: Este método es una alternativa al bucle en el servicio, pero requiere crear una consulta SQL.
     *
     * @param customerId ID del cliente
     */
    @Modifying
    @Query("UPDATE Address a SET a.isBilling = false WHERE a.customer.id = :customerId")
    void unsetBillingForCustomer(@Param("customerId") Long customerId);

    /**
     * Encuentra las direcciones marcadas como dirección de facturación para un cliente específico.
     *
     * @param customerId ID del cliente
     * @return Lista de direcciones marcadas como facturación
     */
    List<Address> findByCustomerIdAndIsBillingTrue(Long customerId);
}
