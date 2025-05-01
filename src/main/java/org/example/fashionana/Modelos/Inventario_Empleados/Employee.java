package org.example.fashionana.Modelos.Inventario_Empleados;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fashionana.Modelos.Pedidos.Order;
import org.example.fashionana.Modelos.Pedidos.OrderStatusHistory;
import org.example.fashionana.Modelos.Pedidos.Return;
import org.example.fashionana.Modelos.Pedidos.Transaction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employees", schema = "BBDD_FashionDAM")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "employee")
    private List<Order> orders;

    @OneToMany(mappedBy = "employee")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "employee")
    private List<Return> returns;

    @OneToMany(mappedBy = "updatedBy")
    private List<OrderStatusHistory> statusHistories;

    @OneToMany(mappedBy = "employee")
    private List<InventoryTransaction> inventoryTransactions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}