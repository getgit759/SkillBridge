// src/main/java/com/skillbridge/model/MentorProfile.java
package com.skillbridge.model;

import com.skillbridge.model.enums.MentorStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "mentor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // References User._id

    private String bio;

    @Indexed // Multikey index for array queries
    private List<String> skills; // e.g., ["Java", "Spring Boot", "MongoDB"]

    private double hourlyRate;

    private int experienceYears;

    private String linkedIn;

    // List of available time slots, e.g., ["MON_10:00", "WED_14:00"]
    private List<String> availability;

    private double avgRating = 0.0;

    private int totalReviews = 0;

    @Builder.Default
    private MentorStatus status = MentorStatus.PENDING;

    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}