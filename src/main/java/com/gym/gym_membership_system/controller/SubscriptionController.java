package com.gym.gym_membership_system.controller;

import com.gym.gym_membership_system.dto.SubscriptionResponse;
import com.gym.gym_membership_system.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // ─── Member — choose a plan and create subscription ───────────────────────
    @PostMapping("/member/subscriptions")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @RequestParam String email,
            @RequestParam Long planId) {
        return ResponseEntity.ok(
                subscriptionService.createSubscription(email, planId));
    }

    // ─── Member — view own subscriptions ─────────────────────────────────────
    @GetMapping("/member/subscriptions/my")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(
            @RequestParam String email) {
        return ResponseEntity.ok(
                subscriptionService.getMySubscriptions(email));
    }

    // ─── Admin — view all subscriptions ──────────────────────────────────────
    @GetMapping("/admin/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    // ─── Admin — cancel a subscription ───────────────────────────────────────
    @PutMapping("/admin/subscriptions/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }
}