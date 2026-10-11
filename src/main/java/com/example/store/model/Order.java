package com.example.store.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private Integer id;
    private String username;
    private Timestamp createdAt;
    private BigDecimal total;
    private String status;
    private String shippingAddress;

    public Order() { }

    public Order(Integer id, String username, Timestamp createdAt, BigDecimal total, String status, String shippingAddress) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
        this.total = total;
        this.status = status;
        this.shippingAddress = shippingAddress;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
