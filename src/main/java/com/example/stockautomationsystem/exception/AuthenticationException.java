package com.example.stockautomationsystem.exception;

//kimlik doğrulama hatasi
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}