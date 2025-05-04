package org.example.fashionana.Controladores.Inventario_Empleados;

import jakarta.servlet.http.HttpSession;
import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Modelos.Inventario_Empleados.InventoryTransaction;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Modelos.Productos.Category;
import org.example.fashionana.Modelos.Productos.Product;
import org.example.fashionana.Modelos.Productos.ProductVariant;
import org.example.fashionana.Servicios.Inventario_Empleados.EmployeeService;
import org.example.fashionana.Servicios.Inventario_Empleados.InventoryService;
import org.example.fashionana.Servicios.Pedidos.OrderService;
import org.example.fashionana.Servicios.Productos.CategoryService;
import org.example.fashionana.Servicios.Productos.ProductService;
import org.example.fashionana.Servicios.Productos.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final CategoryService categoryService;
    private final EmployeeService employeeService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    @Autowired
    public InventoryController(ProductService productService,
                               ProductVariantService productVariantService,
                               CategoryService categoryService,
                               EmployeeService employeeService,
                               InventoryService inventoryService,
                               OrderService orderService) {
        this.productService = productService;
        this.productVariantService = productVariantService;
        this.categoryService = categoryService;
        this.employeeService = employeeService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    @GetMapping
    public String viewInventory(Model model,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) String search) {

        // Obtener todas las categorías para el filtro
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        // Filtrar variantes según los parámetros
        List<ProductVariant> variants;

        if (categoryId != null && categoryId > 0) {
            // Filtrar por categoría
            List<Product> products = productService.findByCategoryId(categoryId);
            variants = products.stream()
                    .flatMap(p -> p.getVariants().stream())
                    .collect(Collectors.toList());
            model.addAttribute("selectedCategory", categoryId);
        } else if (search != null && !search.isEmpty()) {
            // Filtrar por búsqueda de producto
            List<Product> products = productService.findByNameContaining(search);
            variants = products.stream()
                    .flatMap(p -> p.getVariants().stream())
                    .collect(Collectors.toList());
            model.addAttribute("search", search);
        } else {
            // Sin filtros, mostrar todas las variantes
            List<Product> products = productService.findAll();
            variants = products.stream()
                    .flatMap(p -> p.getVariants().stream())
                    .collect(Collectors.toList());
        }

        model.addAttribute("variants", variants);

        return "inventory/list";
    }

    @GetMapping("/edit-stock/{id}")
    public String showEditStockForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // Obtener la variante
        Optional<ProductVariant> variantOpt = productVariantService.findById(id);
        if (!variantOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Variante no encontrada");
            return "redirect:/inventory";
        }

        model.addAttribute("variant", variantOpt.get());
        return "inventory/update-stock";
    }

    @PostMapping("/update-stock")
    public String updateStock(@RequestParam Long variantId,
                              @RequestParam Integer newStock,
                              @RequestParam String operation,
                              @RequestParam String notes,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        // Verificar si el usuario está autenticado como empleado
        Long employeeId = (Long) session.getAttribute("employeeId");
        if (employeeId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe iniciar sesión como empleado");
            return "redirect:/login";
        }

        try {
            // Obtener la variante
            Optional<ProductVariant> variantOpt = productVariantService.findById(variantId);
            if (!variantOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Variante no encontrada");
                return "redirect:/inventory";
            }

            ProductVariant variant = variantOpt.get();
            Integer currentStock = variant.getStockQuantity();

            // Obtener el empleado
            Optional<Employee> employeeOpt = employeeService.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Empleado no encontrado");
                return "redirect:/inventory";
            }

            Employee employee = employeeOpt.get();

            // Calcular la operación según el tipo
            int finalStock = currentStock; // Valor por defecto
            int adjustmentQuantity = 0;

            switch (operation) {
                case "set":
                    adjustmentQuantity = newStock - currentStock;
                    finalStock = newStock;
                    break;
                case "add":
                    adjustmentQuantity = newStock; // La cantidad a añadir
                    finalStock = currentStock + newStock;
                    break;
                case "subtract":
                    adjustmentQuantity = -newStock; // La cantidad a reducir (negativa)
                    finalStock = currentStock - newStock;
                    if (finalStock < 0) {
                        throw new BusinessLogicException("No se puede reducir más stock del disponible");
                    }
                    break;
            }

            // Realizar la operación correspondiente
            if (adjustmentQuantity > 0) {
                // Añadir stock
                inventoryService.addStockToVariant(variantId, adjustmentQuantity, employee, notes);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Stock aumentado en " + adjustmentQuantity + " unidades. Nuevo stock: " + finalStock);
            } else if (adjustmentQuantity < 0) {
                // Reducir stock
                inventoryService.removeStockFromVariant(variantId, Math.abs(adjustmentQuantity), employee, notes);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Stock reducido en " + Math.abs(adjustmentQuantity) + " unidades. Nuevo stock: " + finalStock);
            } else {
                // No hay cambios
                redirectAttributes.addFlashAttribute("successMessage", "No hubo cambios en el stock");
            }

        } catch (BusinessLogicException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/edit-stock/" + variantId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al actualizar el stock: " + e.getMessage());
            return "redirect:/inventory/edit-stock/" + variantId;
        }

        return "redirect:/inventory";
    }




    @GetMapping("/admin/statistics")
    public String showStatistics(Model model) {
        // Cargar transacciones
        List<InventoryTransaction> transactions = inventoryService.findAllTransactions();
        model.addAttribute("transactions", transactions);

        // Cargar empleados para el filtro
        List<Employee> employees = employeeService.findAll();
        model.addAttribute("employees", employees);

        // Cargar estadísticas de ventas
        Map<String, BigDecimal> salesStatistics = inventoryService.getSalesStatistics();
        model.addAttribute("salesStatistics", salesStatistics);

        // Cargar productos más vendidos
        List<Map<String, Object>> topProducts = inventoryService.getTopSellingProducts(10);
        model.addAttribute("topProducts", topProducts);

        // Cargar ingresos totales
        BigDecimal totalRevenue = inventoryService.getTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue);

        // Cargar pedidos recientes
        List<Order> recentOrders = orderService.findRecentOrders(10);
        model.addAttribute("recentOrders", recentOrders);

        return "admin/stadistics";
    }

    @GetMapping("/admin/statistics/filter")
    public String filterTransactions(
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        List<InventoryTransaction> filteredTransactions;

        if (transactionType != null && !transactionType.isEmpty()) {
            filteredTransactions = inventoryService.findTransactionsByType(transactionType);
        } else if (employeeId != null) {
            filteredTransactions = inventoryService.findTransactionsByEmployee(employeeId);
        } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay().minusSeconds(1);
            filteredTransactions = inventoryService.findTransactionsByDateRange(start, end);
        } else {
            filteredTransactions = inventoryService.findAllTransactions();
        }

        model.addAttribute("transactions", filteredTransactions);

        // Volver a cargar el resto de datos para la vista
        return showStatistics(model);
    }
}