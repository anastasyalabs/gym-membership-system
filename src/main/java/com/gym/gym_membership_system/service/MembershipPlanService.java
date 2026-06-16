package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.MembershipPlan;
import com.gym.gym_membership_system.domain.MembershipType;
import com.gym.gym_membership_system.dto.MembershipPlanRequest;
import com.gym.gym_membership_system.dto.MembershipPlanResponse;
import com.gym.gym_membership_system.exception.ResourceNotFoundException;
import com.gym.gym_membership_system.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;

    public MembershipPlanService(MembershipPlanRepository membershipPlanRepository) {
        this.membershipPlanRepository = membershipPlanRepository;
    }

    // ─── Get all active plans (visible to everyone) ──────────────────────────

    public List<MembershipPlanResponse> getAllActivePlans() {
        return membershipPlanRepository.findAll()
                .stream()
                .filter(MembershipPlan::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Get all plans including inactive (admin only) ───────────────────────

    public List<MembershipPlanResponse> getAllPlans() {
        return membershipPlanRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Get single plan by id ───────────────────────────────────────────────

    public MembershipPlanResponse getPlanById(Long id) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership plan not found with id: " + id));
        return toResponse(plan);
    }

    // ─── Admin — create new plan ─────────────────────────────────────────────

    @Transactional
    public MembershipPlanResponse createPlan(MembershipPlanRequest request) {
        MembershipPlan plan = new MembershipPlan(
                request.getMembershipType(),
                request.getName(),
                request.getDescription(),
                request.getDurationDays(),
                request.getPrice(),
                true   // always active when first created
        );
        membershipPlanRepository.save(plan);
        return toResponse(plan);
    }

    // ─── Admin — update existing plan ───────────────────────────────────────

    @Transactional
    public MembershipPlanResponse updatePlan(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership plan not found with id: " + id));

        if (request.getName() != null)
            plan.setName(request.getName());
        if (request.getDescription() != null)
            plan.setDescription(request.getDescription());
        if (request.getMembershipType() != null)
            plan.setMembershipType(request.getMembershipType());
        if (request.getDurationDays() > 0)
            plan.setDurationDays(request.getDurationDays());
        if (request.getPrice() > 0)
            plan.setPrice(request.getPrice());
	if (request.getActive() != null)
	    plan.setActive(request.getActive());

        membershipPlanRepository.save(plan);
        return toResponse(plan);
    }

    // ─── Admin — deactivate plan (BRULE_06 — never hard delete) ─────────────

    @Transactional
    public void deactivatePlan(Long id) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership plan not found with id: " + id));

        // Never delete — set active = false instead
        // This keeps existing subscriptions valid (BRULE_06)
        plan.setActive(false);
        membershipPlanRepository.save(plan);
    }

    // ─── Helper used by SubscriptionService ─────────────────────────────────

    public MembershipPlan getPlanEntityById(Long id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership plan not found with id: " + id));
    }

    // ─── Helper — convert entity to DTO ─────────────────────────────────────

    private MembershipPlanResponse toResponse(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getMembershipType(),
                plan.getName(),
                plan.getDescription(),
                plan.getDurationDays(),
                plan.getPrice(),
                plan.isActive()
        );
    }
}