package org.example.fashionana.Repositorios.Productos;

import org.example.fashionana.Modelos.Productos.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
