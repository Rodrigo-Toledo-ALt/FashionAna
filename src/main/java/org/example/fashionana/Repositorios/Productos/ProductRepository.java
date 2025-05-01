package org.example.fashionana.Repositorios.Productos;

import org.example.fashionana.Modelos.Productos.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String search);

    List<Product> findByCategoryNameIgnoreCase(String categoryName);
}
