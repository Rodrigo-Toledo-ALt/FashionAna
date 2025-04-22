package org.example.fashionana.Controladores;

import org.example.fashionana.Modelos.Productos.Category;
import org.example.fashionana.Modelos.Productos.Product;
import org.example.fashionana.Servicios.Productos.CategoryService;
import org.example.fashionana.Servicios.Productos.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Obtener productos destacados para la página principal
        
        // Obtener categorías para mostrar en la página principal
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        
        return "index";
    }
}