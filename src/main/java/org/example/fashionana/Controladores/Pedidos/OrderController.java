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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                orders = orderService.findByStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                // Si el status no es válido, mostrar todos los pedidos
                orders = orderService.findAll();
            }
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
    public String showOrderForm(
            Model model,
            @RequestParam(required = false) Long customerId) {

        model.addAttribute("order", new Order());

        // Si se especifica un cliente, cargarlo
        if (customerId != null) {
            customerService.findById(customerId).ifPresent(customer ->
                    model.addAttribute("selectedCustomer", customer));
        }

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
    public String getOrdersByCustomer(
            @PathVariable Long customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        // Obtener cliente
        Optional<Customer> customer = customerService.findById(customerId);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
        } else {
            return "redirect:/orders";
        }

        // Lista para almacenar los pedidos tras aplicar filtros
        List<Order> orders;

        // Aplicar filtros si es necesario
        if (status != null) {
            // Filtrar por estado y cliente
            orders = orderService.findAll().stream()
                    .filter(order -> order.getCustomer().getId().equals(customerId) && order.getStatus() == status)
                    .collect(Collectors.toList());
        } else if (search != null && !search.isEmpty()) {
            // Buscar por ID para el cliente específico
            try {
                Long orderId = Long.parseLong(search);
                orders = orderService.findById(orderId)
                        .filter(order -> order.getCustomer().getId().equals(customerId))
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
            } catch (NumberFormatException e) {
                // Si no es un número válido, mostrar todos los pedidos del cliente
                orders = orderService.findByCustomerId(customerId);
            }
        } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            // Filtrar por rango de fechas para el cliente específico
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            orders = orderService.findByOrderDateBetween(
                            start.atStartOfDay(),
                            end.plusDays(1).atStartOfDay().minusSeconds(1)
                    ).stream()
                    .filter(order -> order.getCustomer().getId().equals(customerId))
                    .collect(Collectors.toList());
        } else {
            // Sin filtros, mostrar todos los pedidos del cliente
            orders = orderService.findByCustomerId(customerId);
        }

        // Añadir datos al modelo
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());

        // Mantener los valores de los filtros
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);

        return "orders/by-customer";
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    public String getOrdersByCustomerAndStatus(
            @PathVariable Long customerId,
            @PathVariable OrderStatus status,
            Model model) {
        return getOrdersByCustomer(customerId, status, null, null, null, model);
    }

    @GetMapping("/status/{status}")
    public String getOrdersByStatus(@PathVariable OrderStatus status, Model model) {
        // Redirigir a la vista principal con el filtro de estado aplicado
        return "redirect:/orders?status=" + status;
    }

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus newStatus) {
        orderService.updateOrderStatus(id, newStatus);
        return "redirect:/orders/" + id;
    }
}