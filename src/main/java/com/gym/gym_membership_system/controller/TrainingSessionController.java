package com.gym.gym_membership_system.controller;

import com.gym.gym_membership_system.dto.TrainingSessionRequest;
import com.gym.gym_membership_system.dto.TrainingSessionResponse;
import com.gym.gym_membership_system.service.TrainingSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    public TrainingSessionController(TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    // ─── Public — view available sessions ────────────────────────────────────
    @GetMapping("/public/sessions")
    public ResponseEntity<List<TrainingSessionResponse>> getAvailableSessions() {
        return ResponseEntity.ok(trainingSessionService.getAvailableSessions());
    }

    // ─── Admin — view all sessions ────────────────────────────────────────────
    @GetMapping("/admin/sessions")
    public ResponseEntity<List<TrainingSessionResponse>> getAllSessions() {
        return ResponseEntity.ok(trainingSessionService.getAllSessions());
    }

    // ─── Admin — create session ───────────────────────────────────────────────
    @PostMapping("/admin/sessions")
    public ResponseEntity<TrainingSessionResponse> createSession(
            @RequestBody TrainingSessionRequest request) {
        return ResponseEntity.ok(trainingSessionService.createSession(request));
    }

    // ─── Admin — update session ───────────────────────────────────────────────
    @PutMapping("/admin/sessions/{id}")
    public ResponseEntity<TrainingSessionResponse> updateSession(
            @PathVariable Long id,
            @RequestBody TrainingSessionRequest request) {
        return ResponseEntity.ok(trainingSessionService.updateSession(id, request));
    }

    // ─── Admin — cancel session ───────────────────────────────────────────────
    @DeleteMapping("/admin/sessions/{id}")
    public ResponseEntity<Void> cancelSession(@PathVariable Long id) {
        trainingSessionService.cancelSession(id);
        return ResponseEntity.noContent().build();
    }
}