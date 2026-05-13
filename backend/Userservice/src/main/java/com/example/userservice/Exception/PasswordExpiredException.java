package com.example.userservice.Exception;

public class PasswordExpiredException extends Exception{
    public PasswordExpiredException(String message){
        super(message);
    }
}
