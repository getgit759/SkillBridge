// src/main/java/com/skillbridge/service/EmailService.java
package com.skillbridge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails (confirmations, reminders).
 * All sends are @Async to avoid blocking the request thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendSessionConfirmation(String toEmail, String learnerName,
                                        String mentorName, String topic,
                                        String scheduledAt, String meetingLink) {
        String subject = "SkillBridge – Session Confirmed: " + topic;
        String body = String.format(
                "Hi %s,\n\nYour session with mentor %s on \"%s\" is confirmed!\n\n" +
                        "📅 Date & Time: %s\n🎥 Join here: %s\n\n" +
                        "See you there!\nThe SkillBridge Team",
                learnerName, mentorName, topic, scheduledAt, meetingLink);
        send(toEmail, subject, body);
    }

    @Async
    public void sendSessionReminder(String toEmail, String name,
                                    String topic, String meetingLink) {
        String subject = "SkillBridge – Reminder: Your session starts in 24 hours!";
        String body = String.format(
                "Hi %s,\n\nJust a reminder that your session on \"%s\" is in 24 hours.\n\n" +
                        "🎥 Join here: %s\n\nBe ready to learn!\nThe SkillBridge Team",
                name, topic, meetingLink);
        send(toEmail, subject, body);
    }

    @Async
    public void sendCertificateNotification(String toEmail, String learnerName,
                                            String topic, String certNumber) {
        String subject = "SkillBridge – Your Certificate is Ready! 🎓";
        String body = String.format(
                "Congratulations %s!\n\nYour certificate for completing \"%s\" " +
                        "has been issued.\n\nCertificate #: %s\n\nVisit your dashboard to download it." +
                        "\n\nKeep learning!\nThe SkillBridge Team",
                learnerName, topic, certNumber);
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}