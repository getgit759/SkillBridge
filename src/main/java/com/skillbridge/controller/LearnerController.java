package com.skillbridge.controller;

import com.skillbridge.dto.SessionBookingRequest;
import com.skillbridge.model.*;
import com.skillbridge.model.enums.MentorStatus;
import com.skillbridge.repository.MentorProfileRepository;
import com.skillbridge.repository.UserRepository;
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
@RequestMapping("/learner")
@PreAuthorize("hasRole('LEARNER') or hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class LearnerController {

    private final AuthService authService;
    private final LearnerService learnerService;
    private final SessionService sessionService;
    private final MatchingService matchingService;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    // Add to the fields at the top of LearnerController
    private final CertificateService certificateService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        try {
            User learner = authService.getCurrentUser(userDetails.getUsername());
            List<Session> sessions = learnerService.getSessions(learner.getId());
            List<Certificate> certs = learnerService.getCertificates(learner.getId());

            model.addAttribute("learner", learner);
            model.addAttribute("sessions", sessions);
            model.addAttribute("certificates", certs);
        } catch (Exception e) {
            log.error("Learner dashboard error: {}", e.getMessage(), e);
            model.addAttribute("sessions", List.of());
            model.addAttribute("certificates", List.of());
        }
        return "learner/dashboard";
    }

    @GetMapping("/browse")
    public String browseMentors(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false, defaultValue = "0") double maxBudget,
            Model model) {
        try {
            List<MentorProfile> mentors;
            if (skills != null && !skills.isBlank()) {
                List<String> skillList = Arrays.stream(skills.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
                mentors = matchingService.findBestMatches(skillList, maxBudget, 20);
            } else {
                mentors = mentorProfileRepository.findByStatus(MentorStatus.APPROVED);
            }
            model.addAttribute("mentors", mentors);
            model.addAttribute("userRepository", userRepository);
        } catch (Exception e) {
            log.error("Browse mentors error: {}", e.getMessage(), e);
            model.addAttribute("mentors", List.of());
        }
        return "learner/browse";
    }

    @PostMapping("/book")
    public String bookSession(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute SessionBookingRequest request,
                              Model model) {
        try {
            User learner = authService.getCurrentUser(userDetails.getUsername());
            sessionService.bookSession(learner.getId(), request);
            return "redirect:/learner/dashboard?booked=true";
        } catch (Exception e) {
            log.error("Book session error: {}", e.getMessage(), e);
            return "redirect:/learner/browse?error=true";
        }
    }

    // In LearnerController.java – add @PreAuthorize on the method itself
    @GetMapping("/certificate/{sessionId}/download")
    @PreAuthorize("hasAnyRole('LEARNER', 'ADMIN', 'MENTOR')")  // ← add this
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