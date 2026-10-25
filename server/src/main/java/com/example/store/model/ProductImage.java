package com.example.store.model;

import java.sql.Timestamp;

public class ProductImage {
    private final String contentType;
    private final byte[] data;
    private final Timestamp updatedAt;

    public ProductImage(String contentType, byte[] data, Timestamp updatedAt) {
        this.contentType = contentType;
        this.data = data;
        this.updatedAt = updatedAt;
    }

    public String getContentType() { return contentType; }
    public byte[] getData() { return data; }
    public Timestamp getUpdatedAt() { return updatedAt; }
}
