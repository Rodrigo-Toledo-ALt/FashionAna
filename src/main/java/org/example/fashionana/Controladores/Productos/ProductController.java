package org.example.fashionana.Controladores.Productos;

import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Modelos.Productos.Category;
import org.example.fashionana.Modelos.Productos.Product;
import org.example.fashionana.Modelos.Productos.ProductVariant;
import org.example.fashionana.Servicios.Clientes.CustomerService;
import org.example.fashionana.Servicios.Productos.CategoryService;
import org.example.fashionana.Servicios.Productos.ProductService;
import org.example.fashionana.Servicios.Productos.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductVariantService productVariantService;
    private final CustomerService customerService;

    @Autowired
    public ProductController(ProductService productService, 
                            CategoryService categoryService, 
                            ProductVariantService productVariantService,
                            CustomerService customerService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productVariantService = productVariantService;
        this.customerService = customerService;
    }

    @GetMapping
    public String getAllProducts(
            Model model,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false, defaultValue = "name") String sort) {

        List<Product> products;

        // Aplicar filtros si es necesario
        if (categoryId != null && categoryId > 0) {
            products = productService.findByCategoryId(categoryId);
        } else if (categoryName != null && !categoryName.isEmpty()) {
            // Nuevo método para buscar productos por nombre de categoría
            products = productService.findByCategoryName(categoryName);
        } else if (search != null && !search.isEmpty()) {
            products = productService.findByNameContaining(search);
        } else {
            products = productService.findAll();
        }

        

        // Cargar todas las categorías para el selector de filtro
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);

        return "products/list";
    }

    @GetMapping("/{id}")
    public String getProductDetails(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            
            // If user is an employee, provide list of customers for the cart
            String userType = (String) session.getAttribute("userType");
            if ("employee".equals(userType)) {
                List<Customer> customers = customerService.findAll();
                model.addAttribute("customers", customers);
            }
            
            return "products/details";
        } else {
            return "redirect:/products";
        }
    }

    @GetMapping("/new")
    public String showProductForm(Model model) {
        model.addAttribute("product", new Product());
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "products/form";
    }

    @PostMapping
    public String saveProduct(@ModelAttribute Product product) {
        productService.save(product);
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            List<Category> categories = categoryService.findAll();
            model.addAttribute("categories", categories);
            return "products/form";
        } else {
            return "redirect:/products";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/products";
    }

    @GetMapping("/category/{categoryId}")
    public String getProductsByCategory(@PathVariable Long categoryId, Model model) {
        List<Product> products = productService.findByCategoryId(categoryId);
        model.addAttribute("products", products);
        Optional<Category> category = categoryService.findById(categoryId);
        category.ifPresent(value -> model.addAttribute("category", value));
        return "products/by-category";
    }
    
    @GetMapping("/{productId}/variants/new")
    public String showNewVariantForm(@PathVariable Long productId, Model model) {
        Optional<Product> productOpt = productService.findById(productId);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setPrice(product.getBasePrice()); // Default to base price
            
            model.addAttribute("product", product);
            model.addAttribute("variant", variant);
            model.addAttribute("isNew", true);
            
            return "products/variant-form";
        } else {
            return "redirect:/products";
        }
    }
    
    @GetMapping("/{productId}/variants/{variantId}/edit")
    public String showEditVariantForm(@PathVariable Long productId, 
                                    @PathVariable Long variantId, 
                                    Model model) {
        Optional<Product> productOpt = productService.findById(productId);
        Optional<ProductVariant> variantOpt = productVariantService.findById(variantId);
        
        if (productOpt.isPresent() && variantOpt.isPresent()) {
            ProductVariant variant = variantOpt.get();
            
            // Verify this variant belongs to the specified product
            if (!variant.getProduct().getId().equals(productId)) {
                return "redirect:/products/" + productId;
            }
            
            model.addAttribute("product", productOpt.get());
            model.addAttribute("variant", variant);
            model.addAttribute("isNew", false);
            
            return "products/variant-form";
        } else {
            return "redirect:/products";
        }
    }
    
    @PostMapping("/{productId}/variants")
    public String saveVariant(@PathVariable Long productId,
                             @ModelAttribute ProductVariant variant) {
        // Ensure the variant is linked to the correct product
        Optional<Product> productOpt = productService.findById(productId);
        if (productOpt.isPresent()) {
            variant.setProduct(productOpt.get());
            productVariantService.save(variant);
        }
        
        return "redirect:/products/" + productId + "/edit";
    }
    
    @GetMapping("/{productId}/variants/{variantId}/delete")
    public String deleteVariant(@PathVariable Long productId,
                              @PathVariable Long variantId) {
        // Verify the variant exists and belongs to the product before deleting
        Optional<ProductVariant> variantOpt = productVariantService.findById(variantId);
        if (variantOpt.isPresent() && variantOpt.get().getProduct().getId().equals(productId)) {
            productVariantService.deleteById(variantId);
        }
        
        return "redirect:/products/" + productId + "/edit";
    }
}