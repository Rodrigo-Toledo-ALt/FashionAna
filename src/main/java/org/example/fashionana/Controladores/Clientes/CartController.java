package org.example.fashionana.Controladores.Clientes;

import jakarta.servlet.http.HttpSession;
import org.example.fashionana.DTOs.CartDTO;
import org.example.fashionana.DTOs.CartItemDTO;
import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Clientes.*;
import org.example.fashionana.Modelos.DeliveryType;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Repositorios.Clientes.PaymentMethodRepository;
import org.example.fashionana.Servicios.Clientes.AddressService;
import org.example.fashionana.Servicios.Clientes.CustomerService;
import org.example.fashionana.Servicios.Clientes.ShoppingCartService;
import org.example.fashionana.Servicios.Pedidos.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final ShoppingCartService cartService;
    private final CustomerService customerService;
    private final AddressService addressService;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderService orderService;

    @Autowired
    public CartController(ShoppingCartService cartService,
                          CustomerService customerService,
                          AddressService addressService,
                          PaymentMethodRepository paymentMethodRepository,
                          OrderService orderService) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.addressService = addressService;
        this.paymentMethodRepository = paymentMethodRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {

        // Obtener el ID del cliente desde la sesión
        Long customerId = (Long) session.getAttribute("customerId");

        // Verificar si el usuario ha iniciado sesión
        if (customerId == null) {
            // Si no hay ID de cliente en la sesión, redirigir al login
            return "redirect:/login";
        }

        try {
            ShoppingCart cart = cartService.getCart(customerId);
            model.addAttribute("cart", cart);
            model.addAttribute("customerId", customerId);
            return "cart/view";
        } catch (Exception e) {
            return "redirect:/customers";
        }
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long variantId,
            @RequestParam Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            cartService.addToCart(customerId, variantId, quantity);
            redirectAttributes.addFlashAttribute("message", "Producto añadido al carrito exitosamente");
        } catch (BusinessLogicException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(@RequestParam Long customerId,
                                            @RequestParam Long variantId,
                                            @RequestParam Integer quantity) {
        try {
            // Obtener el carrito actualizado
            ShoppingCart cart = cartService.updateCartItem(customerId, variantId, quantity);

            // Convertir el carrito a DTO
            CartDTO cartDTO = new CartDTO();
            cartDTO.setCustomerId(customerId);
            cartDTO.setTotalItems(cart.getTotalItems());
            cartDTO.setTotalAmount(cart.getTotalAmount());

            // Convertir los items del carrito a DTOs
            List<CartItemDTO> itemDTOs = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                CartItemDTO itemDTO = new CartItemDTO();
                itemDTO.setVariantId(item.getVariant().getId());
                itemDTO.setProductName(item.getVariant().getProduct().getName());
                itemDTO.setSize(item.getVariant().getSize());
                itemDTO.setColor(item.getVariant().getColor());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPrice(item.getPrice());
                itemDTO.setSubtotal(item.getSubtotal());
                itemDTOs.add(itemDTO);
            }
            cartDTO.setItems(itemDTOs);

            return ResponseEntity.ok(cartDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long customerId,
                                 @RequestParam Long variantId,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(customerId, variantId);
            redirectAttributes.addFlashAttribute("message", "Producto eliminado del carrito exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart?customerId=" + customerId;
    }

    @GetMapping("/checkout")
    public String showCheckoutForm(@RequestParam Long customerId, Model model) {
        try {
            Optional<Customer> customer = customerService.findById(customerId);
            if (!customer.isPresent()) {
                return "redirect:/customers";
            }

            ShoppingCart cart = cartService.getCart(customerId);
            if (cart.getItems().isEmpty()) {
                model.addAttribute("error", "Tu carrito esta vacio");
                return "cart/view";
            }

            List<Address> addresses = addressService.findByCustomerId(customerId);
            List<PaymentMethod> paymentMethods = paymentMethodRepository.findByCustomerId(customerId);

            model.addAttribute("cart", cart);
            model.addAttribute("customer", customer.get());
            model.addAttribute("addresses", addresses);
            model.addAttribute("paymentMethods", paymentMethods);
            model.addAttribute("deliveryTypes", DeliveryType.values());

            return "cart/checkout";
        } catch (Exception e) {
            return "redirect:/customers";
        }
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam Long customerId,
                                  @RequestParam Long shippingAddressId,
                                  @RequestParam Long billingAddressId,
                                  @RequestParam Long paymentMethodId,
                                  @RequestParam String deliveryType,
                                  RedirectAttributes redirectAttributes) {
        try {
            Order order = cartService.checkout(customerId, shippingAddressId,
                    billingAddressId, paymentMethodId, deliveryType);
            redirectAttributes.addFlashAttribute("message", "Pedido creado correctamente");
            return "redirect:/orders/" + order.getId();
        } catch (BusinessLogicException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart/checkout?customerId=" + customerId;
        }
    }

    @GetMapping("/clear")
    public String clearCart(@RequestParam Long customerId, RedirectAttributes redirectAttributes) {
        try {
            cartService.clearCart(customerId);
            redirectAttributes.addFlashAttribute("message", "Carrito Limpiado Correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart?customerId=" + customerId;
    }
}
