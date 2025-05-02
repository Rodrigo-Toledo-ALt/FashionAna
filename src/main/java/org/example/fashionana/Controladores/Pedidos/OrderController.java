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

import java.time.LocalDate;
import java.util.Collections;
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
    public String getAllOrders(
            Model model,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Order> orders;

        // Aplicar filtros si es necesario
        if (status != null && !status.isEmpty()) {
            // Filtrar por estado
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            orders = orderService.findByStatus(orderStatus);
        } else if (search != null && !search.isEmpty()) {
            // Buscar por ID o nombre de cliente
            try {
                Long orderId = Long.parseLong(search);
                orders = orderService.findById(orderId)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
            } catch (NumberFormatException e) {
                // Si no es un número, buscar por nombre de cliente
                orders = orderService.findByCustomerNameContaining(search);
            }
        } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            // Filtrar por rango de fechas
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            orders = orderService.findByOrderDateBetween(
                    start.atStartOfDay(),
                    end.plusDays(1).atStartOfDay().minusSeconds(1)
            );
        } else {
            // Sin filtros, mostrar todos los pedidos
            orders = orderService.findAll();
        }

        // Cargar los valores para los filtros
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());

        // Mantener los valores de los filtros para cuando se recargue la página
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);

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
        model.addAttribute("deliveryTypes", org.example.fashionana.Modelos.DeliveryType.values());
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
            model.addAttribute("deliveryTypes", org.example.fashionana.Modelos.DeliveryType.values());
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