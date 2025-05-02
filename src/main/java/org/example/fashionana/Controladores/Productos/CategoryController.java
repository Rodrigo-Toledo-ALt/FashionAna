package org.example.fashionana.Controladores.Productos;

import org.example.fashionana.Modelos.Productos.Category;
import org.example.fashionana.Servicios.Productos.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getAllCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    @GetMapping("/new")
    public String showCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping
    public String saveCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "categories/form";
        } else {
            return "redirect:/categories";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/categories";
    }
}