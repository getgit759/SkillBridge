// src/main/java/com/skillbridge/repository/ReviewRepository.java
package com.skillbridge.repository;

import com.skillbridge.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByRevieweeId(String revieweeId);
    Optional<Review> findBySessionId(String sessionId);
    boolean existsBySessionId(String sessionId);
}