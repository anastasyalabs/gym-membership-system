package com.gym.gym_membership_system.controller;

import com.gym.gym_membership_system.dto.SessionRegistrationResponse;
import com.gym.gym_membership_system.service.SessionRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SessionRegistrationController {

    private final SessionRegistrationService sessionRegistrationService;

    public SessionRegistrationController(
            SessionRegistrationService sessionRegistrationService) {
        this.sessionRegistrationService = sessionRegistrationService;
    }

    // ─── Member — register for a session ─────────────────────────────────────
    @PostMapping("/member/registrations")
    public ResponseEntity<SessionRegistrationResponse> registerForSession(
            @RequestParam String email,
            @RequestParam Long sessionId) {
        return ResponseEntity.ok(
                sessionRegistrationService.registerForSession(email, sessionId));
    }

    // ─── Member — cancel own registration ────────────────────────────────────
    @DeleteMapping("/member/registrations/{id}")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable Long id,
            @RequestParam String email) {
        sessionRegistrationService.cancelRegistration(email, id);
        return ResponseEntity.noContent().build();
    }

    // ─── Member — view own registrations ─────────────────────────────────────
    @GetMapping("/member/registrations/my")
    public ResponseEntity<List<SessionRegistrationResponse>> getMyRegistrations(
            @RequestParam String email) {
        return ResponseEntity.ok(
                sessionRegistrationService.getMyRegistrations(email));
    }

    // ─── Admin — view registrations for a session ────────────────────────────
    @GetMapping("/admin/sessions/{id}/registrations")
    public ResponseEntity<List<SessionRegistrationResponse>> getSessionRegistrations(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                sessionRegistrationService.getRegistrationsForSession(id));
    }
}