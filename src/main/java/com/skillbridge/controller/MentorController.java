package com.skillbridge.controller;

import com.skillbridge.dto.MentorProfileDTO;
import com.skillbridge.model.*;
import com.skillbridge.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mentor")
@PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class MentorController {

    private final AuthService authService;
    private final MentorService mentorService;
    private final SessionService sessionService;
    private final CertificateService certificateService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        try {
            User mentor = authService.getCurrentUser(userDetails.getUsername());
            List<Session> sessions = sessionService.getMentorSessions(mentor.getId());

            MentorProfile profile = null;
            try {
                profile = mentorService.getProfileByUserId(mentor.getId());
            } catch (Exception ignored) {}

            model.addAttribute("mentor", mentor);
            model.addAttribute("profile", profile);
            model.addAttribute("sessions", sessions);
        } catch (Exception e) {
            log.error("Mentor dashboard error: {}", e.getMessage(), e);
            model.addAttribute("sessions", List.of());
        }
        return "mentor/dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        try {
            User mentor = authService.getCurrentUser(userDetails.getUsername());
            MentorProfileDTO dto = new MentorProfileDTO();

            try {
                MentorProfile profile =
                        mentorService.getProfileByUserId(mentor.getId());
                dto.setBio(profile.getBio());
                dto.setSkills(profile.getSkills());
                dto.setHourlyRate(profile.getHourlyRate());
                dto.setExperienceYears(profile.getExperienceYears());
                dto.setLinkedIn(profile.getLinkedIn());
                dto.setAvailability(profile.getAvailability());
            } catch (Exception ignored) {}

            model.addAttribute("mentor", mentor);
            model.addAttribute("profileDTO", dto);
        } catch (Exception e) {
            log.error("Mentor profile page error: {}", e.getMessage(), e);
        }
        return "mentor/profile";
    }

    @PostMapping("/profile")
    public String saveProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(defaultValue = "") String bio,
                              @RequestParam(defaultValue = "") String skillsRaw,
                              @RequestParam(defaultValue = "0") double hourlyRate,
                              @RequestParam(defaultValue = "0") int experienceYears,
                              @RequestParam(defaultValue = "") String linkedIn,
                              @RequestParam(defaultValue = "") String availabilityRaw) {
        try {
            User mentor = authService.getCurrentUser(userDetails.getUsername());
            MentorProfileDTO dto = new MentorProfileDTO();
            dto.setBio(bio);
            dto.setSkills(Arrays.stream(skillsRaw.split(","))
                    .map(String::trim).filter(s -> !s.isBlank())
                    .collect(Collectors.toList()));
            dto.setHourlyRate(hourlyRate);
            dto.setExperienceYears(experienceYears);
            dto.setLinkedIn(linkedIn);
            dto.setAvailability(Arrays.stream(availabilityRaw.split(","))
                    .map(String::trim).filter(s -> !s.isBlank())
                    .collect(Collectors.toList()));
            mentorService.saveProfile(mentor.getId(), dto);
        } catch (Exception e) {
            log.error("Save profile error: {}", e.getMessage(), e);
        }
        return "redirect:/mentor/dashboard?profileSaved=true";
    }

    @PostMapping("/session/{id}/confirm")
    public String confirmSession(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable String id) {
        try {
            User mentor = authService.getCurrentUser(userDetails.getUsername());
            sessionService.confirmSession(id, mentor.getId());
        } catch (Exception e) {
            log.error("Confirm session error: {}", e.getMessage(), e);
        }
        return "redirect:/mentor/dashboard?confirmed=true";
    }

    @PostMapping("/session/{id}/complete")
    public String completeSession(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable String id) {
        try {
            User mentor = authService.getCurrentUser(userDetails.getUsername());
            sessionService.completeSession(id, mentor.getId());
        } catch (Exception e) {
            log.error("Complete session error: {}", e.getMessage(), e);
        }
        return "redirect:/mentor/dashboard?completed=true";
    }

    @GetMapping("/certificate/{sessionId}/download")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable String sessionId) {
        try {
            byte[] pdfBytes = certificateService.getCertificateBytes(sessionId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"certificate-"
                                    + sessionId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Certificate download error: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}