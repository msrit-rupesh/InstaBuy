package com.example.userservice.Exception;

public class InvalidPasswordChangeException extends Exception{
    public InvalidPasswordChangeException(String message){
        super(message);
    }
}
