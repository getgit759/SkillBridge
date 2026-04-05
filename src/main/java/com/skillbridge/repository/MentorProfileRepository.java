// src/main/java/com/skillbridge/repository/MentorProfileRepository.java
package com.skillbridge.repository;

import com.skillbridge.model.MentorProfile;
import com.skillbridge.model.enums.MentorStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MentorProfileRepository extends MongoRepository<MentorProfile, String> {
    Optional<MentorProfile> findByUserId(String userId);
    List<MentorProfile> findByStatus(MentorStatus status);
    List<MentorProfile> findByStatusAndSkillsIn(MentorStatus status, List<String> skills);

    @Query("{ 'status': 'APPROVED', 'skills': { $in: ?0 }, 'hourlyRate': { $lte: ?1 } }")
    List<MentorProfile> findApprovedBySkillsAndMaxRate(List<String> skills, double maxRate);

    long countByStatus(MentorStatus status);
}