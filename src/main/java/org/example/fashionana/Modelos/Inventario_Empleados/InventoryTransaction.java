package org.example.fashionana.Modelos.Inventario_Empleados;



import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.fashionana.Modelos.Productos.ProductVariant;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions", schema = "BBDD_FashionDAM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "variant") // evita recursión inversa
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_transaction_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }
}