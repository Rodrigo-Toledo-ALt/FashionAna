package org.example.fashionana.Repositorios.Productos;

import org.example.fashionana.Modelos.Productos.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}
