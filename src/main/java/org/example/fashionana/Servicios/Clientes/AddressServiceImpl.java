    package org.example.fashionana.Servicios.Clientes;

    import org.example.fashionana.Excepciones.BusinessLogicException;
    import org.example.fashionana.Modelos.Clientes.Address;
    import org.example.fashionana.Modelos.Pedidos.Order;
    import org.example.fashionana.Repositorios.Clientes.AddressRepository;
    import org.example.fashionana.Repositorios.Pedidos.OrderRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.dao.DataIntegrityViolationException;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;
    import java.util.Optional;

    @Service
    @Transactional(readOnly = true)
    public class AddressServiceImpl implements AddressService {

        private final AddressRepository addressRepository;
        private final OrderRepository orderRepository;

        @Autowired
        public AddressServiceImpl(AddressRepository addressRepository, OrderRepository orderRepository) {
            this.addressRepository = addressRepository;
            this.orderRepository = orderRepository;
        }
        @Override
        public List<Address> findAll() {
            return addressRepository.findAll();
        }

        @Override
        public Optional<Address> findById(Long id) {
            return addressRepository.findById(id);
        }

        @Override
        @Transactional
        public Address save(Address address) {
            return addressRepository.save(address);
        }


        @Override
        @Transactional
        public void deleteById(Long id) {
            Optional<Address> addressOpt = addressRepository.findById(id);
            if (addressOpt.isPresent()) {
                Address address = addressOpt.get();

                // Verificar si la dirección está siendo utilizada como dirección de envío en alguna orden
                List<Order> shippingOrders = orderRepository.findByShippingAddressId(id);

                // Verificar si la dirección está siendo utilizada como dirección de facturación en alguna orden
                List<Order> billingOrders = orderRepository.findByBillingAddressId(id);

                if (!shippingOrders.isEmpty() || !billingOrders.isEmpty()) {
                    throw new BusinessLogicException("No se puede eliminar esta dirección porque está siendo utilizada en una o más órdenes.");
                }

                // Si llega aquí, la dirección no está asociada a órdenes y puede ser eliminada
                addressRepository.deleteById(id);
            }
        }

        @Override
        public List<Address> findByCustomerId(Long customerId) {
            return addressRepository.findByCustomerId(customerId);
        }

        @Override
        @Transactional
        public void unsetDefaultForCustomer(Long customerId) {
            addressRepository.unsetDefaultForCustomer(customerId);
        }

        /**
         * Desmarca todas las direcciones de facturación existentes para un cliente específico.
         * Este método debe ser llamado antes de establecer una nueva dirección como dirección de facturación.
         *
         * @param customerId ID del cliente cuyas direcciones de facturación se desmarcarán
         */

        @Transactional
        public void unsetBillingForCustomer(Long customerId) {
            // Utilizamos el método del repositorio que hace una actualización en masa
            addressRepository.unsetBillingForCustomer(customerId);
        }

    }