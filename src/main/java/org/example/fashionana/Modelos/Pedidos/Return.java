package org.example.fashionana.Modelos.Pedidos;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fashionana.Modelos.Clientes.Customer;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "returns", schema = "BBDD_FashionDAM")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "status", length = 50)
    private String status;

    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL)
    private List<ReturnItem> returnItems;

    @PrePersist
    protected void onCreate() {
        returnDate = LocalDateTime.now();
        status = "pending";
    }
}