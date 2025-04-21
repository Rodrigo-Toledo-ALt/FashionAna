package org.example.fashionana.Modelos.Clientes;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fashionana.Modelos.Pedidos.Order;

import java.util.List;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "account_number", length = 255)
    private String accountNumber;

    @Column(name = "expiry_date", length = 10)
    private String expiryDate;

    @Column(name = "is_default")
    private Boolean isDefault;

    @OneToMany(mappedBy = "paymentMethod")
    private List<Order> orders;
}