// src/main/java/com/skillbridge/service/MentorService.java
package com.skillbridge.service;

import com.skillbridge.dto.MentorProfileDTO;
import com.skillbridge.dto.ReviewRequest;
import com.skillbridge.exception.ResourceNotFoundException;
import com.skillbridge.model.*;
import com.skillbridge.model.enums.MentorStatus;
import com.skillbridge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Create or update a mentor's profile.
     */
    public MentorProfile saveProfile(String userId, MentorProfileDTO dto) {
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElse(MentorProfile.builder()
                        .userId(userId)
                        .status(MentorStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build());

        profile.setBio(dto.getBio());
        profile.setSkills(dto.getSkills());
        profile.setHourlyRate(dto.getHourlyRate());
        profile.setExperienceYears(dto.getExperienceYears());
        profile.setLinkedIn(dto.getLinkedIn());
        profile.setAvailability(dto.getAvailability());

        return mentorProfileRepository.save(profile);
    }

    public MentorProfile getProfileByUserId(String userId) {
        return mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mentor profile not found for user: " + userId));
    }

    public List<MentorProfile> getApprovedMentors() {
        return mentorProfileRepository.findByStatus(MentorStatus.APPROVED);
    }

    /**
     * Submit a review and update mentor's average rating.
     */
    public Review submitReview(String reviewerId, ReviewRequest req) {
        if (reviewRepository.existsBySessionId(req.getSessionId())) {
            throw new IllegalStateException("Review already submitted for this session.");
        }

        // Get mentorId from session – delegated to SessionService via the controller.
        // Here we assume revieweeId is passed as part of ReviewRequest.
        Review review = Review.builder()
                .sessionId(req.getSessionId())
                .reviewerId(reviewerId)
                .revieweeId(req.getComment()) // placeholder – set properly in controller
                .rating(req.getRating())
                .comment(req.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        recalculateMentorRating(review.getRevieweeId());
        return saved;
    }

    /**
     * Recalculate and persist mentor's average rating.
     */
    public void recalculateMentorRating(String mentorUserId) {
        List<Review> reviews = reviewRepository.findByRevieweeId(mentorUserId);
        OptionalDouble avg = reviews.stream().mapToInt(Review::getRating).average();

        mentorProfileRepository.findByUserId(mentorUserId).ifPresent(profile -> {
            profile.setAvgRating(avg.orElse(0.0));
            profile.setTotalReviews(reviews.size());
            mentorProfileRepository.save(profile);
            log.info("Updated rating for mentor {}: {}", mentorUserId, profile.getAvgRating());
        });
    }

    public List<MentorProfile> searchBySkills(List<String> skills) {
        return mentorProfileRepository.findByStatusAndSkillsIn(MentorStatus.APPROVED, skills);
    }
}