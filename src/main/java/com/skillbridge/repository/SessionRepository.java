// src/main/java/com/skillbridge/repository/SessionRepository.java
package com.skillbridge.repository;

import com.skillbridge.model.Session;
import com.skillbridge.model.enums.SessionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByLearnerId(String learnerId);
    List<Session> findByMentorId(String mentorId);
    List<Session> findByLearnerIdAndStatus(String learnerId, SessionStatus status);
    List<Session> findByMentorIdAndStatus(String mentorId, SessionStatus status);
    List<Session> findByStatus(SessionStatus status);

    // For reminder scheduler: sessions scheduled within next 24h, reminder not yet sent
    @Query("{ 'scheduledAt': { $gte: ?0, $lte: ?1 }, 'reminderSent': false, 'status': 'CONFIRMED' }")
    List<Session> findUpcomingSessionsForReminder(LocalDateTime from, LocalDateTime to);

    long countByStatus(SessionStatus status);
    long countByMentorId(String mentorId);

    // Count repeat users (learners with more than 1 completed session)
    @Query(value = "{ 'status': 'COMPLETED' }", count = true)
    long countCompletedSessions();
}