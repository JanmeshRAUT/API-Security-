package com.apisecurity.starter.exception;

public class ApiSecurityException extends RuntimeException {
    
    public ApiSecurityException(String message) {
        super(message);
    }
    
    public ApiSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
