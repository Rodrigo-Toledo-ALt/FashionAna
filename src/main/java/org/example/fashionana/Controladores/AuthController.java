package org.example.fashionana.Controladores;

import jakarta.servlet.http.HttpSession;
import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Servicios.Clientes.CustomerService;
import org.example.fashionana.Servicios.Inventario_Empleados.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final CustomerService customerService;
    private final EmployeeService employeeService;

    @Autowired
    public AuthController(CustomerService customerService, EmployeeService employeeService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login/customer")
    public String customerLogin(@RequestParam String email, @RequestParam String password,
                                HttpSession session, Model model) {
        Optional<Customer> customer = customerService.findByEmail(email);

        if (customer.isPresent() && customerService.checkPassword(password, customer.get().getPassword())) {
            // Inicio de sesión exitoso
            session.setAttribute("customerId", customer.get().getId());
            session.setAttribute("userType", "customer");
            session.setAttribute("userName", customer.get().getFirstName() + " " + customer.get().getLastName());
            return "redirect:/customer/dashboard";
        } else {
            // Credenciales incorrectas
            model.addAttribute("error", "Credenciales incorrectas");
            return "login";
        }
    }

    @PostMapping("/login/employee")
    public String employeeLogin(@RequestParam String email, @RequestParam String password,
                                HttpSession session, Model model) {
        Optional<Employee> employee = employeeService.findByEmail(email);

        if (employee.isPresent() && employeeService.checkPassword(password, employee.get().getPassword())) {
            // Inicio de sesión exitoso
            session.setAttribute("employeeId", employee.get().getId());
            session.setAttribute("userType", "employee");
            session.setAttribute("userName", employee.get().getFirstName() + " " + employee.get().getLastName());
            session.setAttribute("role", employee.get().getRole());
            return "redirect:/admin/dashboard";
        } else {
            // Credenciales incorrectas
            model.addAttribute("error", "Credenciales incorrectas");
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(Customer customer, Model model) {
        try {
            // Verificar si el email ya existe
            Optional<Customer> existingCustomer = customerService.findByEmail(customer.getEmail());
            if (existingCustomer.isPresent()) {
                model.addAttribute("error", "El email ya está registrado");
                return "register";
            }

            customerService.save(customer);
            model.addAttribute("success", "Registro exitoso. Por favor, inicie sesión.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}