package org.example.fashionana.Controladores.Clientes;

import org.example.fashionana.Excepciones.BusinessLogicException;
import org.example.fashionana.Modelos.Clientes.Address;
import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Servicios.Clientes.AddressService;
import org.example.fashionana.Servicios.Clientes.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AddressService addressService;

    @Autowired
    public CustomerController(CustomerService customerService, AddressService addressService) {
        this.customerService = customerService;
        this.addressService = addressService;
    }

    @GetMapping
    public String getAllCustomers(Model model) {
        List<Customer> customers = customerService.findAll();
        model.addAttribute("customers", customers);
        return "customers/list";
    }

    @GetMapping("/{id}")
    public String getCustomerDetails(@PathVariable Long id, Model model) {
        Optional<Customer> customer = customerService.findById(id);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            return "customers/details";
        } else {
            return "redirect:/customers";
        }
    }

    @GetMapping("/new")
    public String showCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @PostMapping
    public String saveCustomer(@ModelAttribute Customer customer) {
        customerService.save(customer);
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Customer> customer = customerService.findById(id);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            return "customers/form";
        } else {
            return "redirect:/customers";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente eliminado correctamente.");
        } catch (BusinessLogicException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customers";
    }

    // MÉTODOS PARA GESTIONAR DIRECCIONES

    /**
     * Muestra el formulario para crear una nueva dirección
     */
    // Endpoint para mostrar el formulario de nueva dirección
    @GetMapping("/{customerId}/addresses/new")
    public String showNewAddressForm(@PathVariable Long customerId, Model model) {
        Optional<Customer> customerOpt = customerService.findById(customerId);
        if (customerOpt.isEmpty()) {
            return "redirect:/customers";
        }

        Customer customer = customerOpt.get();
        Address address = new Address();
        address.setCustomer(customer);

        // Obtener las direcciones existentes para mostrar información al usuario
        List<Address> existingAddresses = addressService.findByCustomerId(customerId);

        // Verificar si ya existe una dirección de facturación
        boolean hasBillingAddress = existingAddresses.stream()
                .anyMatch(addr -> Boolean.TRUE.equals(addr.getIsBilling()));

        model.addAttribute("customer", customer);
        model.addAttribute("address", address);
        model.addAttribute("isNew", true);
        model.addAttribute("hasBillingAddress", hasBillingAddress);

        return "customers/address-form";
    }

    /**
     * Guarda una nueva dirección
     */
    @PostMapping("/{customerId}/addresses")
    public String saveAddress(@PathVariable Long customerId, @ModelAttribute Address address) {
        Optional<Customer> customer = customerService.findById(customerId);
        if (customer.isPresent()) {
            address.setCustomer(customer.get());

            // Si es la primera dirección o se marca como predeterminada, establecerla como predeterminada
            if (customer.get().getAddresses().isEmpty() || Boolean.TRUE.equals(address.getIsDefault())) {
                address.setIsDefault(true);
                // Desmarcar otras direcciones como predeterminadas si es necesario
                if (!customer.get().getAddresses().isEmpty()) {
                    addressService.unsetDefaultForCustomer(customerId);
                }
            }
            // Verificar si esta dirección se está marcando como dirección de facturación
            if (Boolean.TRUE.equals(address.getIsBilling())) {
                // Desactivar cualquier otra dirección de facturación
                addressService.unsetBillingForCustomer(customerId);
            }

            addressService.save(address);
            return "redirect:/customers/" + customerId;
        } else {
            return "redirect:/customers";
        }
    }

    /**
     * Muestra el formulario para editar una dirección existente
     */
    @GetMapping("/{customerId}/addresses/{addressId}/edit")
    public String showEditAddressForm(@PathVariable Long customerId,
                                      @PathVariable Long addressId,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerService.findById(customerId);
        Optional<Address> addressOpt = addressService.findById(addressId);

        if (customerOpt.isEmpty() || addressOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente o dirección no encontrados");
            return "redirect:/customers";
        }

        Address currentAddress = addressOpt.get();

        // Verificar si existe otra dirección de facturación (excluyendo esta dirección)
        boolean otherBillingAddressExists = addressService.findByCustomerId(customerId).stream()
                .filter(addr -> !addr.getId().equals(addressId))
                .anyMatch(addr -> Boolean.TRUE.equals(addr.getIsBilling()));



        model.addAttribute("customer", customerOpt.get());
        model.addAttribute("address", currentAddress);
        model.addAttribute("isNew", false);
        model.addAttribute("isBillingAddress", Boolean.TRUE.equals(currentAddress.getIsBilling()));
        model.addAttribute("otherBillingAddressExists", otherBillingAddressExists);

        return "customers/address-form";
    }

    /**
     * Actualiza una dirección existente
     */
    @PostMapping("/{customerId}/addresses/{addressId}")
    public String updateAddress(@PathVariable Long customerId, @PathVariable Long addressId, @ModelAttribute Address address) {
        Optional<Address> existingAddress = addressService.findById(addressId);
        Optional<Customer> customer = customerService.findById(customerId);

        if (existingAddress.isPresent() && customer.isPresent() && existingAddress.get().getCustomer().getId().equals(customerId)) {
            address.setId(addressId);
            address.setCustomer(customer.get());

            // Si se marca como predeterminada, desmarcar otras direcciones
            if (Boolean.TRUE.equals(address.getIsDefault()) && !Boolean.TRUE.equals(existingAddress.get().getIsDefault())) {
                addressService.unsetDefaultForCustomer(customerId);
            }

            // Si se marca como dirección de facturación, desmarcar otras direcciones de facturación
            if (Boolean.TRUE.equals(address.getIsBilling()) && !Boolean.TRUE.equals(existingAddress.get().getIsBilling())) {
                addressService.unsetBillingForCustomer(customerId);
            }

            addressService.save(address);
            return "redirect:/customers/" + customerId;
        } else {
            return "redirect:/customers";
        }
    }

    /**
     * Elimina una dirección
     */
    @PostMapping("/{customerId}/addresses/{addressId}/delete")
    public String deleteAddress(@PathVariable Long customerId, @PathVariable Long addressId, RedirectAttributes redirectAttributes) {
        Optional<Address> address = addressService.findById(addressId);

        if (address.isPresent() && address.get().getCustomer().getId().equals(customerId)) {
            try {
                boolean wasDefault = Boolean.TRUE.equals(address.get().getIsDefault());
                addressService.deleteById(addressId);

                // Si la dirección eliminada era la predeterminada, establecer otra como predeterminada
                if (wasDefault) {
                    Optional<Customer> customer = customerService.findById(customerId);
                    if (customer.isPresent() && !customer.get().getAddresses().isEmpty()) {
                        Address newDefault = customer.get().getAddresses().get(0);
                        newDefault.setIsDefault(true);
                        addressService.save(newDefault);
                    }
                }

                redirectAttributes.addFlashAttribute("successMessage", "Dirección eliminada con éxito.");

            } catch (BusinessLogicException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }

            return "redirect:/customers/" + customerId;
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Dirección no encontrada o no pertenece al cliente especificado.");
            return "redirect:/customers";
        }
    }
    /**
     * Establece una dirección como predeterminada
     */
    @GetMapping("/{customerId}/addresses/{addressId}/setDefault")
    public String setDefaultAddress(@PathVariable Long customerId, @PathVariable Long addressId) {
        Optional<Address> address = addressService.findById(addressId);

        if (address.isPresent() && address.get().getCustomer().getId().equals(customerId)) {
            // Desmarcar todas las direcciones predeterminadas actuales
            addressService.unsetDefaultForCustomer(customerId);

            // Establecer esta dirección como predeterminada
            Address defaultAddress = address.get();
            defaultAddress.setIsDefault(true);
            addressService.save(defaultAddress);

            return "redirect:/customers/" + customerId;
        } else {
            return "redirect:/customers";
        }
    }
}