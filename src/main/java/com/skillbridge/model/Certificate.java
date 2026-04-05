// src/main/java/com/skillbridge/model/Certificate.java
package com.skillbridge.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sessionId;

    private String learnerId;
    private String mentorId;
    private String learnerName;
    private String mentorName;
    private String topic;
    private String certificateNumber;
    private String filePath;
    private LocalDateTime issuedAt;
}