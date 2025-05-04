package org.example.fashionana.Servicios.Inventario_Empleados;

import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Modelos.Inventario_Empleados.InventoryTransaction;
import org.example.fashionana.Modelos.Productos.Product;
import org.example.fashionana.Modelos.Productos.ProductVariant;
import org.example.fashionana.Repositorios.Inventario_Empleados.InventoryTransactionRepository;
import org.example.fashionana.Repositorios.Productos.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductVariantRepository productVariantRepository;

    @Autowired
    public InventoryServiceImpl(
            InventoryTransactionRepository inventoryTransactionRepository,
            ProductVariantRepository productVariantRepository) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    @Transactional
    public InventoryTransaction addStockToVariant(Long variantId, Integer quantity,
                                                  Employee employee, String notes) {
        if (quantity <= 0) {
            throw new BusinessLogicException("La cantidad debe ser mayor que cero");
        }

        Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
        if (!variantOpt.isPresent()) {
            throw new BusinessLogicException("Variante no encontrada");
        }

        ProductVariant variant = variantOpt.get();

        // Crear la transacción de inventario
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setVariant(variant);
        transaction.setQuantity(quantity);
        transaction.setTransactionType("Purchase");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setEmployee(employee);
        transaction.setNotes(notes);

        // Actualizar el stock de la variante
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);

        return inventoryTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public InventoryTransaction removeStockFromVariant(Long variantId, Integer quantity,
                                                       Employee employee, String notes) {
        if (quantity <= 0) {
            throw new BusinessLogicException("La cantidad debe ser mayor que cero");
        }

        Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
        if (!variantOpt.isPresent()) {
            throw new BusinessLogicException("Variante no encontrada");
        }

        ProductVariant variant = variantOpt.get();

        if (variant.getStockQuantity() < quantity) {
            throw new BusinessLogicException("No hay suficiente stock disponible. Stock actual: "
                    + variant.getStockQuantity());
        }

        // Crear la transacción de inventario
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setVariant(variant);
        transaction.setQuantity(-quantity); // Cantidad negativa para disminución
        transaction.setTransactionType("Adjustment");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setEmployee(employee);
        transaction.setNotes(notes);

        // Actualizar el stock de la variante
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        productVariantRepository.save(variant);

        return inventoryTransactionRepository.save(transaction);
    }

    // Implementación en InventoryServiceImpl
    @Override
    public List<InventoryTransaction> findAllTransactions() {
        return inventoryTransactionRepository.findAll(Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Override
    public List<InventoryTransaction> findTransactionsByType(String transactionType) {
        // Convertir el tipo de transacción a la forma correcta (primera letra mayúscula, resto minúsculas)
        String formattedType = transactionType.substring(0, 1).toUpperCase() +
                transactionType.substring(1).toLowerCase();

        return inventoryTransactionRepository.findByTransactionType(formattedType,
                Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Override
    public List<InventoryTransaction> findTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return inventoryTransactionRepository.findByTransactionDateBetween(start, end,
                Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Override
    public List<InventoryTransaction> findTransactionsByEmployee(Long employeeId) {
        return inventoryTransactionRepository.findByEmployeeId(employeeId,
                Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Override
    public Map<String, BigDecimal> getSalesStatistics() {
        Map<String, BigDecimal> statistics = new HashMap<>();

        // Calcular ventas diarias, semanales y mensuales
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        BigDecimal dailySales = calculateSalesForPeriod(startOfDay, now);
        BigDecimal weeklySales = calculateSalesForPeriod(startOfWeek, now);
        BigDecimal monthlySales = calculateSalesForPeriod(startOfMonth, now);

        statistics.put("daily", dailySales);
        statistics.put("weekly", weeklySales);
        statistics.put("monthly", monthlySales);

        return statistics;
    }

    private BigDecimal calculateSalesForPeriod(LocalDateTime start, LocalDateTime end) {
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findByTransactionDateBetween(
                        start, end, Sort.by(Sort.Direction.DESC, "transactionDate"))
                .stream()
                .filter(t -> "Sale".equals(t.getTransactionType()))
                .collect(Collectors.toList());

        return transactions.stream()
                .map(t -> {
                    if (t.getVariant() != null) {
                        return t.getVariant().getPrice().multiply(new BigDecimal(Math.abs(t.getQuantity())));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        // Obtener transacciones de venta
        List<InventoryTransaction> salesTransactions = inventoryTransactionRepository
                .findByTransactionType("Sale", Sort.by(Sort.Direction.DESC, "transactionDate"));

        // Agrupar por producto y calcular cantidades vendidas e ingresos
        Map<ProductVariant, Integer> variantQuantities = new HashMap<>();
        Map<ProductVariant, BigDecimal> variantRevenues = new HashMap<>();

        for (InventoryTransaction transaction : salesTransactions) {
            ProductVariant variant = transaction.getVariant();
            if (variant != null) {
                int quantity = Math.abs(transaction.getQuantity());
                BigDecimal revenue = variant.getPrice().multiply(new BigDecimal(quantity));

                variantQuantities.put(variant, variantQuantities.getOrDefault(variant, 0) + quantity);
                variantRevenues.put(variant, variantRevenues.getOrDefault(variant, BigDecimal.ZERO).add(revenue));
            }
        }

        // Consolidar por producto (agrupando variantes)
        Map<Product, Integer> productQuantities = new HashMap<>();
        Map<Product, BigDecimal> productRevenues = new HashMap<>();

        for (Map.Entry<ProductVariant, Integer> entry : variantQuantities.entrySet()) {
            Product product = entry.getKey().getProduct();
            productQuantities.put(product, productQuantities.getOrDefault(product, 0) + entry.getValue());
        }

        for (Map.Entry<ProductVariant, BigDecimal> entry : variantRevenues.entrySet()) {
            Product product = entry.getKey().getProduct();
            productRevenues.put(product, productRevenues.getOrDefault(product, BigDecimal.ZERO).add(entry.getValue()));
        }

        // Convertir a lista de mapas para devolverla
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product product : productQuantities.keySet()) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", product.getId());
            productData.put("name", product.getName());
            productData.put("soldQuantity", productQuantities.get(product));
            productData.put("revenue", productRevenues.get(product));
            result.add(productData);
        }

        // Ordenar por cantidad vendida en orden descendente
        result.sort((a, b) -> ((Integer) b.get("soldQuantity")).compareTo((Integer) a.get("soldQuantity")));

        // Limitar resultados
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }

    @Override
    public BigDecimal getTotalRevenue() {
        List<InventoryTransaction> salesTransactions = inventoryTransactionRepository
                .findByTransactionType("Sale", Sort.by(Sort.Direction.DESC, "transactionDate"));

        return salesTransactions.stream()
                .map(t -> {
                    if (t.getVariant() != null) {
                        return t.getVariant().getPrice().multiply(new BigDecimal(Math.abs(t.getQuantity())));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}