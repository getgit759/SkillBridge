package com.skillbridge.controller;

import com.skillbridge.dto.AuthResponse;
import com.skillbridge.dto.LoginRequest;
import com.skillbridge.dto.RegisterRequest;
import com.skillbridge.model.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
                        Model model,
                        HttpServletResponse response) {
        try {
            AuthResponse auth = authService.login(request, response);
            return switch (auth.getRole()) {
                case "ROLE_ADMIN"  -> "redirect:/admin/dashboard";
                case "ROLE_MENTOR" -> "redirect:/mentor/dashboard";
                default            -> "redirect:/learner/dashboard";
            };
        } catch (Exception e) {
            log.error("Login controller error: {}", e.getMessage(), e);
            model.addAttribute("error", "Login failed: " + e.getMessage());
            model.addAttribute("loginRequest", request);
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           Model model) {
        try {
            authService.register(request);
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerRequest", request);
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        authService.logout(response);
        return "redirect:/auth/login?logout=true";
    }

    // ── Temporary debug endpoint – DELETE after login is working ─────────────
    @GetMapping("/debug")
    @ResponseBody
    public String debug(@RequestParam String email,
                        @RequestParam String password) {
        try {
            Optional<User> userOpt =
                    userRepository.findByEmail(email.trim().toLowerCase());

            if (userOpt.isEmpty()) {
                return "❌ STEP 1 FAILED: No user found with email: "
                        + email.trim().toLowerCase();
            }

            User user = userOpt.get();
            boolean passwordMatch =
                    passwordEncoder.matches(password, user.getPassword());

            if (!passwordMatch) {
                return "❌ STEP 2 FAILED: Password does NOT match."
                        + " | Active: " + user.isActive()
                        + " | Role: " + user.getRole();
            }

            return "✅ ALL OK: User=" + user.getEmail()
                    + " | Role=" + user.getRole()
                    + " | Active=" + user.isActive()
                    + " | Password matches=true";

        } catch (Exception e) {
            return "❌ EXCEPTION: " + e.getMessage();
        }
    }
}