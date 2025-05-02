package org.example.fashionana.Servicios.Productos;

import org.example.fashionana.Modelos.Productos.ProductVariant;

import java.util.List;
import java.util.Optional;

public interface ProductVariantService {
    List<ProductVariant> findAll();
    Optional<ProductVariant> findById(Long id);
    ProductVariant save(ProductVariant productVariant);
    void deleteById(Long id);
    List<ProductVariant> findByProductId(Long productId);
}