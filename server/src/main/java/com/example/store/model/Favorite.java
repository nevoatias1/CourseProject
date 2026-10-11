package com.example.store.model;

public class Favorite {
    private String username;
    private Integer productId;

    public Favorite() { }

    public Favorite(String username, Integer productId) {
        this.username = username;
        this.productId = productId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
}
