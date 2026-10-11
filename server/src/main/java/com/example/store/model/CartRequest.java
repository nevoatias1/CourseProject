package com.example.store.model;

public class CartRequest {
    private Integer productId;
    private Integer quantity;

    public CartRequest() { }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
