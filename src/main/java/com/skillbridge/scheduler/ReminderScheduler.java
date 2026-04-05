// src/main/java/com/skillbridge/scheduler/ReminderScheduler.java
package com.skillbridge.scheduler;

import com.skillbridge.model.Session;
import com.skillbridge.model.User;
import com.skillbridge.repository.SessionRepository;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scans confirmed sessions scheduled in the next 24 hours
 * and fires reminder emails every hour.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Run every hour. Find sessions starting 23–25 hours from now
     * whose reminders haven't been sent yet.
     */
    @Scheduled(fixedRate = 3_600_000) // every 1 hour
    public void sendUpcomingReminders() {
        LocalDateTime from = LocalDateTime.now().plusHours(23);
        LocalDateTime to   = LocalDateTime.now().plusHours(25);

        List<Session> upcoming = sessionRepository
                .findUpcomingSessionsForReminder(from, to);

        log.info("Reminder check: {} sessions in the 24h window.", upcoming.size());

        for (Session session : upcoming) {
            try {
                sendReminderToParticipant(session.getLearnerId(), session);
                sendReminderToParticipant(session.getMentorId(), session);

                session.setReminderSent(true);
                sessionRepository.save(session);
            } catch (Exception e) {
                log.error("Reminder failed for session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    private void sendReminderToParticipant(String userId, Session session) {
        userRepository.findById(userId).ifPresent(user ->
                emailService.sendSessionReminder(
                        user.getEmail(),
                        user.getName(),
                        session.getTopic(),
                        session.getMeetingLink()));
    }
}