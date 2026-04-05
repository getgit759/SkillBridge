// src/main/java/com/skillbridge/dto/AnalyticsDTO.java
package com.skillbridge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsDTO {
    private long totalMentors;
    private long totalLearners;
    private long activeSessions;
    private long completedSessions;
    private long pendingApprovals;
    private double avgRating;
    private long totalCertificatesIssued;
}