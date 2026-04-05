// src/main/java/com/skillbridge/model/Review.java
package com.skillbridge.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sessionId;

    private String reviewerId;   // Learner's userId
    private String revieweeId;   // Mentor's userId

    private int rating;          // 1 – 5
    private String comment;

    @CreatedDate
    private LocalDateTime createdAt;
}