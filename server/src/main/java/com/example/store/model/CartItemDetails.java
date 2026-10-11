package com.example.store.model;

import java.math.BigDecimal;

public class CartItemDetails {
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal lineTotal;

    public CartItemDetails() { }

    public CartItemDetails(Integer productId, String productName, BigDecimal price, String imageUrl, Integer quantity, BigDecimal lineTotal) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
