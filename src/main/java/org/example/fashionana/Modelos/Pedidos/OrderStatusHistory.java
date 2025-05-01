package org.example.fashionana.Modelos.Pedidos;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Modelos.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history", schema = "BBDD_FashionDAM")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private Employee updatedBy;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    public void setChangedAt(LocalDateTime now) {
        this.updatedAt = now;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void setOldStatus(OrderStatus oldStatus) {
        // Como no existe un campo oldStatus en la clase,
        // podríamos guardarlo en las notas
        if (this.notes == null) {
            this.notes = "Status anterior: " + oldStatus;
        } else {
            this.notes = "Status anterior: " + oldStatus + ". " + this.notes;
        }
    }
}