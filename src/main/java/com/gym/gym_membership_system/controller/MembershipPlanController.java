package com.gym.gym_membership_system.controller;

import com.gym.gym_membership_system.dto.MembershipPlanRequest;
import com.gym.gym_membership_system.dto.MembershipPlanResponse;
import com.gym.gym_membership_system.service.MembershipPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    public MembershipPlanController(MembershipPlanService membershipPlanService) {
        this.membershipPlanService = membershipPlanService;
    }

    // ─── Public — view active plans ──────────────────────────────────────────
    @GetMapping("/public/plans")
    public ResponseEntity<List<MembershipPlanResponse>> getActivePlans() {
        return ResponseEntity.ok(membershipPlanService.getAllActivePlans());
    }

    // ─── Admin — view all plans including inactive ────────────────────────────
    @GetMapping("/admin/plans")
    public ResponseEntity<List<MembershipPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(membershipPlanService.getAllPlans());
    }

    // ─── Admin — create plan ──────────────────────────────────────────────────
    @PostMapping("/admin/plans")
    public ResponseEntity<MembershipPlanResponse> createPlan(
            @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.ok(membershipPlanService.createPlan(request));
    }

    // ─── Admin — update plan ──────────────────────────────────────────────────
    @PutMapping("/admin/plans/{id}")
    public ResponseEntity<MembershipPlanResponse> updatePlan(
            @PathVariable Long id,
            @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.ok(membershipPlanService.updatePlan(id, request));
    }

    // ─── Admin — deactivate plan ──────────────────────────────────────────────
    @DeleteMapping("/admin/plans/{id}")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long id) {
        membershipPlanService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }
}