package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_sku")
public class ProductSkuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(nullable=false)
    private Long price;

    @Column(name="stock_on_hand", nullable=false)
    private Integer stockOnHand;

    @Column(name="reserved_stock", nullable=false)
    private Integer reservedStock;

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getPrice() { return price; }
    public Integer getStockOnHand() { return stockOnHand; }
    public Integer getReservedStock() { return reservedStock; }

    public void setStockOnHand(Integer stockOnHand){ this.stockOnHand = stockOnHand; }
    public void setReservedStock(Integer reservedStock) { this.reservedStock = reservedStock; }
}
