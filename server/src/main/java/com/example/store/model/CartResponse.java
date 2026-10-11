package com.example.store.model;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private List<CartItemDetails> items;
    private BigDecimal total;

    public CartResponse() { }

    public CartResponse(List<CartItemDetails> items, BigDecimal total) {
        this.items = items;
        this.total = total;
    }

    public List<CartItemDetails> getItems() { return items; }
    public void setItems(List<CartItemDetails> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
