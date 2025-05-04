package org.example.fashionana.Repositorios.Inventario_Empleados;

import org.example.fashionana.Modelos.Inventario_Empleados.InventoryTransaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * Encuentra todas las transacciones para una variante específica
     * @param variantId ID de la variante
     * @param sort Ordenación de los resultados
     * @return Lista de transacciones
     */
    List<InventoryTransaction> findByVariantId(Long variantId, Sort sort);

    /**
     * Encuentra todas las transacciones realizadas por un empleado
     * @param employeeId ID del empleado
     * @param sort Ordenación de los resultados
     * @return Lista de transacciones
     */
    List<InventoryTransaction> findByEmployeeId(Long employeeId, Sort sort);

    /**
     * Encuentra transacciones de un tipo específico
     * @param transactionType Tipo de transacción
     * @param sort Ordenación de los resultados
     * @return Lista de transacciones
     */
    List<InventoryTransaction> findByTransactionType(String transactionType, Sort sort);

    /**
     * Encuentra transacciones en un rango de fechas
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @param sort Ordenación de los resultados
     * @return Lista de transacciones
     */
    List<InventoryTransaction> findByTransactionDateBetween(LocalDateTime startDate,
                                                            LocalDateTime endDate,
                                                            Sort sort);

    /**
     * Encuentra transacciones relacionadas con un pedido específico
     * @param referenceId ID de referencia (ID del pedido)
     * @return Lista de transacciones
     */
    List<InventoryTransaction> findByReferenceId(Integer referenceId);

    /**
     * Obtiene un resumen de las transacciones agrupadas por tipo
     * @return Lista de resúmenes por tipo
     */
    @Query("SELECT t.transactionType, SUM(t.quantity) FROM InventoryTransaction t GROUP BY t.transactionType")
    List<Object[]> getTransactionSummaryByType();

    List<InventoryTransaction> findByTransactionTypeAndTransactionDateBetween(String sale, LocalDateTime start, LocalDateTime end);
}