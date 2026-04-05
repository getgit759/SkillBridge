// src/main/java/com/skillbridge/listener/SessionEventListener.java
package com.skillbridge.listener;

import com.skillbridge.event.SessionCompletedEvent;
import com.skillbridge.model.Certificate;
import com.skillbridge.model.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.CertificateService;
import com.skillbridge.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionEventListener {

    private final CertificateService certificateService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Triggered automatically when a session is marked COMPLETED.
     * Generates a PDF certificate and notifies the learner.
     */
    @EventListener
    @Async
    public void onSessionCompleted(SessionCompletedEvent event) {
        String sessionId = event.getSession().getId();
        log.info("Handling SessionCompletedEvent for session {}", sessionId);

        try {
            Certificate cert = certificateService.generateCertificate(sessionId);

            // Notify learner via email
            userRepository.findById(event.getSession().getLearnerId())
                    .ifPresent(learner -> emailService.sendCertificateNotification(
                            learner.getEmail(),
                            learner.getName(),
                            event.getSession().getTopic(),
                            cert.getCertificateNumber()));

        } catch (Exception e) {
            log.error("Certificate generation failed for session {}: {}", sessionId, e.getMessage());
        }
    }
}