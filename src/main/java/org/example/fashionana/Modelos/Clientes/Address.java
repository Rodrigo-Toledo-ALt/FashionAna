package org.example.fashionana.Modelos.Clientes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fashionana.Modelos.Pedidos.Order;

import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "is_billing")
    private Boolean isBilling;

    @OneToMany(mappedBy = "shippingAddress")
    private List<Order> shippingOrders;

    @OneToMany(mappedBy = "billingAddress")
    private List<Order> billingOrders;

}
