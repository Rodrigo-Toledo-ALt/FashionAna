package org.example.fashionana.Servicios.Productos;

import org.example.fashionana.Modelos.Productos.ProductVariant;
import org.example.fashionana.Repositorios.Productos.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;

    @Autowired
    public ProductVariantServiceImpl(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public List<ProductVariant> findAll() {
        return productVariantRepository.findAll();
    }

    @Override
    public Optional<ProductVariant> findById(Long id) {
        return productVariantRepository.findById(id);
    }

    @Override
    @Transactional
    public ProductVariant save(ProductVariant productVariant) {
        return productVariantRepository.save(productVariant);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        productVariantRepository.deleteById(id);
    }

    @Override
    public List<ProductVariant> findByProductId(Long productId) {
        return productVariantRepository.findAll().stream()
                .filter(variant -> variant.getProduct() != null &&
                                  variant.getProduct().getId().equals(productId))
                .collect(Collectors.toList());
    }
}