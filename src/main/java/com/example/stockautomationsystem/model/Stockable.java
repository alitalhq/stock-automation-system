package com.example.stockautomationsystem.model;

public interface Stockable {
    void reduceStock(int quantity);
    boolean hasEnoughStock(int quantity);
}