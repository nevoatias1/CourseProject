package com.example.store.model;

public class CartItem {
    private String username;
    private Integer productId;
    private Integer quantity;

    public CartItem() { }

    public CartItem(String username, Integer productId, Integer quantity) {
        this.username = username;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
