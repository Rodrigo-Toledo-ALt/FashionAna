package org.example.fashionana.Controladores.Pedidos;

import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Modelos.OrderStatus;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Servicios.Clientes.CustomerService;
import org.example.fashionana.Servicios.Pedidos.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;

    @Autowired
    public OrderController(OrderService orderService, CustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @GetMapping
    public String getAllOrders(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String getOrderDetails(@PathVariable Long id, Model model) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            return "orders/details";
        } else {
            return "redirect:/orders";
        }
    }

    @GetMapping("/new")
    public String showOrderForm(Model model) {
        model.addAttribute("order", new Order());
        List<Customer> customers = customerService.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/form";
    }

    @PostMapping
    public String saveOrder(@ModelAttribute Order order) {
        orderService.save(order);
        return "redirect:/orders";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            List<Customer> customers = customerService.findAll();
            model.addAttribute("customers", customers);
            model.addAttribute("statuses", OrderStatus.values());
            return "orders/form";
        } else {
            return "redirect:/orders";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteById(id);
        return "redirect:/orders";
    }

    @GetMapping("/customer/{customerId}")
    public String getOrdersByCustomer(@PathVariable Long customerId, Model model) {
        List<Order> orders = orderService.findByCustomerId(customerId);
        model.addAttribute("orders", orders);
        Optional<Customer> customer = customerService.findById(customerId);
        customer.ifPresent(value -> model.addAttribute("customer", value));
        return "orders/by-customer";
    }

    @GetMapping("/status/{status}")
    public String getOrdersByStatus(@PathVariable OrderStatus status, Model model) {
        List<Order> orders = orderService.findByStatus(status);
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        return "orders/by-status";
    }

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus newStatus) {
        orderService.updateOrderStatus(id, newStatus);
        return "redirect:/orders/" + id;
    }
}