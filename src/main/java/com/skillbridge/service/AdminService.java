package com.skillbridge.service;

import com.skillbridge.dto.AnalyticsDTO;
import com.skillbridge.exception.ResourceNotFoundException;
import com.skillbridge.model.MentorProfile;
import com.skillbridge.model.enums.MentorStatus;
import com.skillbridge.model.enums.Role;
import com.skillbridge.model.enums.SessionStatus;
import com.skillbridge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final CertificateRepository certificateRepository;

    public List<MentorProfile> getPendingMentors() {
        try {
            return mentorProfileRepository.findByStatus(MentorStatus.PENDING);
        } catch (Exception e) {
            log.error("getPendingMentors error: {}", e.getMessage());
            return List.of();
        }
    }

    public MentorProfile approveMentor(String profileId) {
        MentorProfile profile = mentorProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found: " + profileId));
        profile.setStatus(MentorStatus.APPROVED);
        return mentorProfileRepository.save(profile);
    }

    public MentorProfile rejectMentor(String profileId) {
        MentorProfile profile = mentorProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found: " + profileId));
        profile.setStatus(MentorStatus.REJECTED);
        return mentorProfileRepository.save(profile);
    }

    public AnalyticsDTO getAnalytics() {
        try {
            long totalMentors = safeCount(
                    () -> userRepository.countByRole(Role.ROLE_MENTOR));
            long totalLearners = safeCount(
                    () -> userRepository.countByRole(Role.ROLE_LEARNER));
            long activeSessions = safeCount(
                    () -> sessionRepository.countByStatus(SessionStatus.CONFIRMED))
                    + safeCount(
                    () -> sessionRepository.countByStatus(SessionStatus.ONGOING));
            long completedSessions = safeCount(
                    () -> sessionRepository.countByStatus(SessionStatus.COMPLETED));
            long pendingApprovals = safeCount(
                    () -> mentorProfileRepository.countByStatus(MentorStatus.PENDING));
            long totalCerts = safeCount(() -> certificateRepository.count());

            double avgRating = 0.0;
            try {
                avgRating = mentorProfileRepository
                        .findByStatus(MentorStatus.APPROVED).stream()
                        .filter(m -> m.getTotalReviews() > 0)
                        .mapToDouble(MentorProfile::getAvgRating)
                        .average().orElse(0.0);
                avgRating = Math.round(avgRating * 10.0) / 10.0;
            } catch (Exception e) {
                log.warn("avgRating calculation failed: {}", e.getMessage());
            }

            return AnalyticsDTO.builder()
                    .totalMentors(totalMentors)
                    .totalLearners(totalLearners)
                    .activeSessions(activeSessions)
                    .completedSessions(completedSessions)
                    .pendingApprovals(pendingApprovals)
                    .avgRating(avgRating)
                    .totalCertificatesIssued(totalCerts)
                    .build();

        } catch (Exception e) {
            log.error("getAnalytics error: {}", e.getMessage(), e);
            // Return zeroed analytics instead of crashing
            return AnalyticsDTO.builder()
                    .totalMentors(0).totalLearners(0)
                    .activeSessions(0).completedSessions(0)
                    .pendingApprovals(0).avgRating(0.0)
                    .totalCertificatesIssued(0)
                    .build();
        }
    }

    // Helper to safely call count queries without crashing
    private long safeCount(java.util.concurrent.Callable<Long> supplier) {
        try {
            return supplier.call();
        } catch (Exception e) {
            log.warn("Count query failed: {}", e.getMessage());
            return 0L;
        }
    }
}