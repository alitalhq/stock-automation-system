package com.example.stockautomationsystem.model;

import com.example.stockautomationsystem.exception.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Product extends BaseProduct implements Stockable, Loggable {
    private int stockQuantity;

    public Product() {
    }

    public Product(String name, String category, double price, int stockQuantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    @Override
    public void reduceStock(int quantity) {
        if (quantity > this.stockQuantity) {
            throw new InsufficientStockException(this.name, quantity, this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    @Override
    public boolean hasEnoughStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    @Override
    public String generateProductCode() {
        return "PRD-" + name.hashCode();
    }

    @Override
    @JsonIgnore
    public String getLogDetails() {
        return String.format("Ürün: %s | Kat: %s | Fiyat: %.2f | Stok: %d", name, category, price, stockQuantity);
    }
}