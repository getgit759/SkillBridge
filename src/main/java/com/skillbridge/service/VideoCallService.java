// src/main/java/com/skillbridge/service/VideoCallService.java
package com.skillbridge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Video call integration.
 *
 * Strategy A: Jitsi Meet (open source, zero credentials needed).
 * Strategy B: Zoom API (set zoom.api-key in application.yml).
 *
 * The service defaults to Jitsi; switch to Zoom by setting
 *   video.provider=zoom in application.yml.
 */
@Service
@Slf4j
public class VideoCallService {

    @Value("${jitsi.base-url:https://meet.jit.si}")
    private String jitsiBaseUrl;

    @Value("${zoom.api-key:}")
    private String zoomApiKey;

    @Value("${video.provider:jitsi}")
    private String videoProvider;

    /**
     * Generate a unique meeting link for the session.
     *
     * @param sessionId unique session identifier
     * @return meeting URL that both mentor and learner can open
     */
    public String createMeetingLink(String sessionId) {
        if ("zoom".equalsIgnoreCase(videoProvider) && !zoomApiKey.isBlank()) {
            return createZoomMeeting(sessionId);
        }
        return createJitsiRoom(sessionId);
    }

    /**
     * Jitsi: simply construct a room URL – no API call needed.
     * Room name is deterministic so both parties get the same URL.
     */
    private String createJitsiRoom(String sessionId) {
        String roomName = "skillbridge-" + sessionId;
        String link = jitsiBaseUrl + "/" + roomName;
        log.info("Jitsi room created: {}", link);
        return link;
    }

    /**
     * Zoom: create a meeting via REST API and return join_url.
     *
     * To enable:
     *   1. Go to https://marketplace.zoom.us → Build Server-to-Server OAuth App
     *   2. Add meeting:write:admin scope
     *   3. Set zoom.api-key and zoom.api-secret in application.yml
     *   4. Replace the stub below with actual HTTP call using RestTemplate/WebClient
     */
    private String createZoomMeeting(String sessionId) {
        // STUB – replace with real Zoom REST call
        log.warn("Zoom API stub invoked for session {}. Provide real credentials.", sessionId);
        return "https://zoom.us/j/mock-" + sessionId;
    }
}