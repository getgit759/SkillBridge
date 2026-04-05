// src/main/java/com/skillbridge/service/LearnerService.java
package com.skillbridge.service;

import com.skillbridge.model.Certificate;
import com.skillbridge.model.Session;
import com.skillbridge.repository.CertificateRepository;
import com.skillbridge.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearnerService {

    private final SessionRepository sessionRepository;
    private final CertificateRepository certificateRepository;

    public List<Session> getSessions(String learnerId) {
        return sessionRepository.findByLearnerId(learnerId);
    }

    public List<Certificate> getCertificates(String learnerId) {
        return certificateRepository.findByLearnerId(learnerId);
    }
}