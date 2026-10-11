package com.example.store.model;

import java.math.BigDecimal;

public class Product {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    private Integer categoryId;
    private String categoryName;

    public Product() { }

    public Product(Integer id, String name, String description, BigDecimal price, String imageUrl, Integer stock, Integer categoryId) {
        this(id, name, description, price, imageUrl, stock, categoryId, null);
    }

    public Product(Integer id, String name, String description, BigDecimal price, String imageUrl, Integer stock, Integer categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
