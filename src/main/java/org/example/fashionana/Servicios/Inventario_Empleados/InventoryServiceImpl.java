package org.example.fashionana.Servicios.Inventario_Empleados;

import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Modelos.Inventario_Empleados.InventoryTransaction;
import org.example.fashionana.Modelos.Productos.ProductVariant;
import org.example.fashionana.Repositorios.Inventario_Empleados.InventoryTransactionRepository;
import org.example.fashionana.Repositorios.Productos.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
}