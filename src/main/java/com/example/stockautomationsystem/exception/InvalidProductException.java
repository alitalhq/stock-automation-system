package com.example.stockautomationsystem.exception;

//gecersiz ürün hatasi
public class InvalidProductException extends Exception {
    public InvalidProductException(String message) {
        super(message);
    }
}