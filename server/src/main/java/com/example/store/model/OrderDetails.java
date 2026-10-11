package com.example.store.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class OrderDetails {
    private Integer id;
    private Timestamp createdAt;
    private BigDecimal total;
    private String status;
    private String shippingAddress;
    private List<OrderItemDetails> items;

    public OrderDetails() { }

    public OrderDetails(Integer id, Timestamp createdAt, BigDecimal total, String status, String shippingAddress, List<OrderItemDetails> items) {
        this.id = id;
        this.createdAt = createdAt;
        this.total = total;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.items = items;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public List<OrderItemDetails> getItems() { return items; }
    public void setItems(List<OrderItemDetails> items) { this.items = items; }
}
