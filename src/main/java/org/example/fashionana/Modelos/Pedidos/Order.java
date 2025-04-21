package org.example.fashionana.Modelos.Pedidos;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Modelos.Clientes.Address;
import org.example.fashionana.Modelos.Clientes.PaymentMethod;
import org.example.fashionana.Modelos.DeliveryType;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Modelos.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType;

    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    @ManyToOne
    @JoinColumn(name = "billing_address_id")
    private Address billingAddress;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private PaymentMethod paymentMethod;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "order")
    private List<Return> returns;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderStatusHistory> statusHistories;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        status = OrderStatus.CREATED;
    }
}