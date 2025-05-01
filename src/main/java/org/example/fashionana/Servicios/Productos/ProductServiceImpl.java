package org.example.fashionana.Servicios.Productos;

import org.example.fashionana.Modelos.Productos.Product;
import org.example.fashionana.Repositorios.Productos.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)  // Aplica transacciones de solo lectura por defecto
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = productRepository.findAll();
        System.out.println("Productos encontrados: " + products.size());
        // Si hay productos, imprime algunos detalles del primero
        if (!products.isEmpty()) {
            System.out.println("Primer producto: " + products.get(0).getName());
        } else {
            System.out.println("No se encontraron productos");
        }
        return products;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional  // Anula readOnly para permitir escritura
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional  // Anula readOnly para permitir escritura
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {

        return productRepository.findAll().stream()
                .filter(product -> product.getCategory() != null &&
                                   product.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());

    }

    @Override
    public int countAll() {
        return (int) productRepository.count();  // Más eficiente que findAll().size()
    }

    @Override
    public List<Product> findByNameContaining(String search) {
        return productRepository.findByNameContainingIgnoreCase(search);
    }

    @Override
    public List<Product> findByCategoryName(String categoryName) {
        return productRepository.findByCategoryNameIgnoreCase(categoryName);
    }
}