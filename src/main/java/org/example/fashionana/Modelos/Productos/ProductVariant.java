package org.example.fashionana.Modelos.Productos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.fashionana.Modelos.Inventario_Empleados.InventoryTransaction;
import org.example.fashionana.Modelos.Pedidos.OrderItem;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product_variants", schema = "BBDD_FashionDAM")
@Data
@NoArgsConstructor
@ToString(exclude = "inventoryTransactions") // evita recursión
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "size", nullable = false, length = 10)
    private String size;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @OneToMany(mappedBy = "variant")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "variant")
    private List<InventoryTransaction> inventoryTransactions;
}