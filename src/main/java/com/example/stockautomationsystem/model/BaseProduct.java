package com.example.stockautomationsystem.model;

public abstract class BaseProduct {
    protected String name;
    protected String category;
    protected double price;

    public abstract String generateProductCode();
}