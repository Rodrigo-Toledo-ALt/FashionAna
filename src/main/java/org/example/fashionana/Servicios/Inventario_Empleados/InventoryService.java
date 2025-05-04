package org.example.fashionana.Servicios.Inventario_Empleados;

import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Modelos.Inventario_Empleados.InventoryTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InventoryService {

    /**
     * Añade cantidad al stock de una variante y registra la transacción
     * @param variantId ID de la variante
     * @param quantity Cantidad a añadir
     * @param employee Empleado que realiza la operación
     * @param notes Notas adicionales
     * @return La transacción de inventario creada
     */
    InventoryTransaction addStockToVariant(Long variantId, Integer quantity, Employee employee, String notes);

    /**
     * Reduce cantidad del stock de una variante y registra la transacción
     * @param variantId ID de la variante
     * @param quantity Cantidad a reducir
     * @param employee Empleado que realiza la operación
     * @param notes Notas adicionales
     * @return La transacción de inventario creada
     */
    InventoryTransaction removeStockFromVariant(Long variantId, Integer quantity, Employee employee, String notes);


    List<InventoryTransaction> findAllTransactions();
    List<InventoryTransaction> findTransactionsByType(String transactionType);
    List<InventoryTransaction> findTransactionsByDateRange(LocalDateTime start, LocalDateTime end);
    List<InventoryTransaction> findTransactionsByEmployee(Long employeeId);
    Map<String, BigDecimal> getSalesStatistics(); // Ventas por período (diarias, semanales, mensuales)
    List<Map<String, Object>> getTopSellingProducts(int limit);
    BigDecimal getTotalRevenue();
}