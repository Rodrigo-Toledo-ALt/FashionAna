package org.example.fashionana.Repositorios.Inventario_Empleados;

import org.example.fashionana.Modelos.Inventario_Empleados.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
}
