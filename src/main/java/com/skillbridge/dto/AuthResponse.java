// src/main/java/com/skillbridge/dto/AuthResponse.java
package com.skillbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String name;
    private String userId;
}