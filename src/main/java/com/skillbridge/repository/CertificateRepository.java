// src/main/java/com/skillbridge/repository/CertificateRepository.java
package com.skillbridge.repository;

import com.skillbridge.model.Certificate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends MongoRepository<Certificate, String> {
    Optional<Certificate> findBySessionId(String sessionId);
    List<Certificate> findByLearnerId(String learnerId);
}