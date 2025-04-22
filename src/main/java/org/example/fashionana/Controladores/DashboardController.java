package org.example.fashionana.Controladores;

import jakarta.servlet.http.HttpSession;
import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Modelos.Productos.Product;
import org.example.fashionana.Servicios.Clientes.CustomerService;
import org.example.fashionana.Servicios.Inventario_Empleados.EmployeeService;
import org.example.fashionana.Servicios.Pedidos.OrderService;
import org.example.fashionana.Servicios.Productos.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public DashboardController(CustomerService customerService, EmployeeService employeeService,
                              OrderService orderService, ProductService productService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.orderService = orderService;
        this.productService = productService;
    }



    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        // Verificar si el usuario está autenticado como cliente
        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        // Obtener información del cliente
        Optional<Customer> customer = customerService.findById(customerId);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            
            // Obtener pedidos recientes del cliente
            List<Order> recentOrders = orderService.findRecentOrdersByCustomerId(customerId, 5);
            model.addAttribute("recentOrders", recentOrders);
        }

        return "customer/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        // Verificar si el usuario está autenticado como empleado
        Long employeeId = (Long) session.getAttribute("employeeId");
        if (employeeId == null) {
            return "redirect:/login";
        }

        // Obtener información del empleado
        Optional<Employee> employee = employeeService.findById(employeeId);
        if (employee.isPresent()) {
            model.addAttribute("employee", employee.get());
            
            // Obtener estadísticas para el dashboard
            int totalCustomers = customerService.countAll();
            int totalOrders = orderService.countAll();
            int totalProducts = productService.countAll();
            
            model.addAttribute("totalCustomers", totalCustomers);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalProducts", totalProducts);
            
            // Obtener pedidos recientes para mostrar en el dashboard
            List<Order> recentOrders = orderService.findRecentOrders(10);
            model.addAttribute("recentOrders", recentOrders);
        }

        return "admin/dashboard";
    }
}