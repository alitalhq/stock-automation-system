package com.example.stockautomationsystem.exception;

//veri kayit hatasi
public class DataPersistenceException extends RuntimeException {
    public DataPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}