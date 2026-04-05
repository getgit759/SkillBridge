// src/main/java/com/skillbridge/model/Session.java
package com.skillbridge.model;

import com.skillbridge.model.enums.SessionStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    private String id;

    private String mentorId;      // References User._id
    private String learnerId;     // References User._id
    private String mentorProfileId;

    private LocalDateTime scheduledAt;
    private int durationMinutes;

    private String topic;
    private String meetingLink;
    private String meetingId;

    @Builder.Default
    private SessionStatus status = SessionStatus.PENDING;

    // Payment fields
    private String paymentStatus; // "PENDING", "PAID", "MOCK_PAID"
    private double amount;

    private boolean reminderSent = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}