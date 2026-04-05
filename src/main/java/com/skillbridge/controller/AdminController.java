package com.skillbridge.controller;

import com.skillbridge.dto.AnalyticsDTO;
import com.skillbridge.model.MentorProfile;
import com.skillbridge.service.AdminService;
import com.skillbridge.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final MatchingService matchingService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            AnalyticsDTO analytics = adminService.getAnalytics();
            List<MentorProfile> pending = adminService.getPendingMentors();
            List<MentorProfile> topRated = matchingService.getTopRatedMentors(5);

            model.addAttribute("analytics", analytics);
            model.addAttribute("pendingMentors", pending);
            model.addAttribute("topRatedMentors", topRated);
        } catch (Exception e) {
            log.error("Admin dashboard error: {}", e.getMessage(), e);
            model.addAttribute("analytics", AnalyticsDTO.builder()
                    .totalMentors(0).totalLearners(0).activeSessions(0)
                    .completedSessions(0).pendingApprovals(0)
                    .avgRating(0.0).totalCertificatesIssued(0).build());
            model.addAttribute("pendingMentors", List.of());
            model.addAttribute("topRatedMentors", List.of());
        }
        return "admin/dashboard";
    }

    @PostMapping("/mentor/{id}/approve")
    public String approveMentor(@PathVariable String id) {
        try {
            adminService.approveMentor(id);
        } catch (Exception e) {
            log.error("Approve mentor error: {}", e.getMessage());
        }
        return "redirect:/admin/dashboard?approved=true";
    }

    @PostMapping("/mentor/{id}/reject")
    public String rejectMentor(@PathVariable String id) {
        try {
            adminService.rejectMentor(id);
        } catch (Exception e) {
            log.error("Reject mentor error: {}", e.getMessage());
        }
        return "redirect:/admin/dashboard?rejected=true";
    }
}