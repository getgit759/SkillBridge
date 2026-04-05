// src/main/java/com/skillbridge/dto/RegisterRequest.java
package com.skillbridge.dto;

import com.skillbridge.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6)
    private String password;

    private Role role; // ROLE_LEARNER or ROLE_MENTOR
}