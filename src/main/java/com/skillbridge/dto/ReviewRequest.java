// src/main/java/com/skillbridge/dto/ReviewRequest.java
package com.skillbridge.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
public class ReviewRequest {
    private String sessionId;
    @Min(1) @Max(5)
    private int rating;
    private String comment;
}