// src/main/java/com/skillbridge/exception/UnauthorizedException.java
package com.skillbridge.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}