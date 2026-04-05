// src/main/java/com/skillbridge/dto/SessionBookingRequest.java
package com.skillbridge.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionBookingRequest {
    private String mentorId;
    private String topic;
    private LocalDateTime scheduledAt;
    private int durationMinutes;
}