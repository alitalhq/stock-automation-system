package com.example.stockautomationsystem.exception;

//kullanıcı zaten var hatasi
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("The username '" + username + "' is already taken. Please choose another one.");
    }
}