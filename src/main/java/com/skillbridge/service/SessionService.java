// src/main/java/com/skillbridge/service/SessionService.java
package com.skillbridge.service;

import com.skillbridge.dto.SessionBookingRequest;
import com.skillbridge.event.SessionCompletedEvent;
import com.skillbridge.exception.ResourceNotFoundException;
import com.skillbridge.model.Session;
import com.skillbridge.model.enums.SessionStatus;
import com.skillbridge.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final VideoCallService videoCallService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Learner books a session with a mentor.
     * Payment is mocked (set paymentStatus = "MOCK_PAID").
     */
    public Session bookSession(String learnerId, SessionBookingRequest req) {
        // Get mentor profile for amount
        String meetingLink = videoCallService.createMeetingLink(
                UUID.randomUUID().toString().substring(0, 8));

        Session session = Session.builder()
                .learnerId(learnerId)
                .mentorId(req.getMentorId())
                .topic(req.getTopic())
                .scheduledAt(req.getScheduledAt())
                .durationMinutes(req.getDurationMinutes())
                .status(SessionStatus.PENDING)
                .meetingLink(meetingLink)
                .paymentStatus("MOCK_PAID") // Mock payment
                .amount(0) // Set actual amount via mentor profile in controller
                .createdAt(LocalDateTime.now())
                .build();

        Session saved = sessionRepository.save(session);
        log.info("Session booked: {} by learner {} with mentor {}",
                saved.getId(), learnerId, req.getMentorId());
        return saved;
    }

    /**
     * Mentor confirms a session request.
     */
    public Session confirmSession(String sessionId, String mentorId) {
        Session session = getSessionById(sessionId);
        validateMentorOwnership(session, mentorId);
        session.setStatus(SessionStatus.CONFIRMED);
        return sessionRepository.save(session);
    }

    /**
     * Mark session as COMPLETED and publish event for certificate generation.
     */
    public Session completeSession(String sessionId, String mentorId) {
        Session session = getSessionById(sessionId);
        validateMentorOwnership(session, mentorId);
        session.setStatus(SessionStatus.COMPLETED);
        Session saved = sessionRepository.save(session);

        // Publish event – triggers certificate generation via @EventListener
        eventPublisher.publishEvent(new SessionCompletedEvent(this, saved));
        log.info("Session {} completed, certificate event published.", sessionId);
        return saved;
    }

    public Session cancelSession(String sessionId, String userId) {
        Session session = getSessionById(sessionId);
        if (!session.getMentorId().equals(userId) && !session.getLearnerId().equals(userId)) {
            throw new IllegalStateException("Not authorized to cancel this session.");
        }
        session.setStatus(SessionStatus.CANCELLED);
        return sessionRepository.save(session);
    }

    public Session getSessionById(String id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + id));
    }

    public List<Session> getLearnerSessions(String learnerId) {
        return sessionRepository.findByLearnerId(learnerId);
    }

    public List<Session> getMentorSessions(String mentorId) {
        return sessionRepository.findByMentorId(mentorId);
    }

    private void validateMentorOwnership(Session session, String mentorId) {
        if (!session.getMentorId().equals(mentorId)) {
            throw new IllegalStateException("Not authorized for this session.");
        }
    }
}