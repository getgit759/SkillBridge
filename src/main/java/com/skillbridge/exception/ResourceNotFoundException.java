// src/main/java/com/skillbridge/exception/ResourceNotFoundException.java
package com.skillbridge.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}