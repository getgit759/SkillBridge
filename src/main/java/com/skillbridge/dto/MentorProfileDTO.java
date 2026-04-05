// src/main/java/com/skillbridge/dto/MentorProfileDTO.java
package com.skillbridge.dto;

import lombok.Data;
import java.util.List;

@Data
public class MentorProfileDTO {
    private String bio;
    private List<String> skills;
    private double hourlyRate;
    private int experienceYears;
    private String linkedIn;
    private List<String> availability; // e.g., ["MON_10:00", "WED_14:00"]
}