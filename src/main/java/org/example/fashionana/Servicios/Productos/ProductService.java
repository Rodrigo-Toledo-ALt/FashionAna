package org.example.fashionana.Servicios.Productos;

import org.example.fashionana.Modelos.Productos.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAll();
    Optional<Product> findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
    List<Product> findByCategoryId(Long categoryId);

    int countAll();
}