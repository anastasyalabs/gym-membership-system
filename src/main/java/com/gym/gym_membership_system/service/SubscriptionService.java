package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.Member;
import com.gym.gym_membership_system.domain.MembershipPlan;
import com.gym.gym_membership_system.domain.MembershipStatus;
import com.gym.gym_membership_system.domain.Subscription;
import com.gym.gym_membership_system.dto.SubscriptionResponse;
import com.gym.gym_membership_system.exception.ResourceNotFoundException;
import com.gym.gym_membership_system.exception.SubscriptionNotActiveException;
import com.gym.gym_membership_system.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberService memberService;
    private final MembershipPlanService membershipPlanService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               MemberService memberService,
                               MembershipPlanService membershipPlanService) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberService = memberService;
        this.membershipPlanService = membershipPlanService;
    }

    // ─── Member — create subscription by choosing a plan ─────────────────────

    @Transactional
    public SubscriptionResponse createSubscription(String email, Long planId) {
        // 1. Get the member from logged-in email
        Member member = memberService.getMemberEntityByEmail(email);

        // 2. Get the plan they chose
        MembershipPlan plan = membershipPlanService.getPlanEntityById(planId);

        // 3. Check plan is still active
        if (!plan.isActive()) {
            throw new IllegalArgumentException(
                    "This membership plan is no longer available");
        }

        // 4. Calculate dates
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        // 5. Create and save subscription
        Subscription subscription = new Subscription(
                member,
                plan,
                startDate,
                endDate,
                MembershipStatus.ACTIVE
        );
        subscriptionRepository.save(subscription);

        return toResponse(subscription);
    }

    // ─── Member/Admin — view subscriptions for logged-in member ──────────────

    public List<SubscriptionResponse> getMySubscriptions(String email) {
        Member member = memberService.getMemberEntityByEmail(email);
        return subscriptionRepository.findByMember(member)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Admin — view all subscriptions ──────────────────────────────────────

    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Admin — manually cancel a subscription (BRULE_05) ───────────────────

    @Transactional
    public SubscriptionResponse cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + subscriptionId));

        if (subscription.getStatus() == MembershipStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Subscription is already cancelled");
        }

        subscription.setStatus(MembershipStatus.CANCELLED);
        subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }

    // ─── Check if member has an active subscription (used by SessionRegistrationService) ──

    public void checkMemberHasActiveSubscription(Member member) {
        List<Subscription> subscriptions = subscriptionRepository.findByMember(member);

        // Find any active subscription
        boolean hasActive = subscriptions.stream()
                .anyMatch(s -> s.getStatus() == MembershipStatus.ACTIVE);

        if (!hasActive) {
            // Check why — give a specific error message
            boolean hasCancelled = subscriptions.stream()
                    .anyMatch(s -> s.getStatus() == MembershipStatus.CANCELLED);

            if (hasCancelled) {
                throw new SubscriptionNotActiveException(
                        "Subscription is cancelled. Registration is not allowed.");  // E04
            }

            throw new SubscriptionNotActiveException(
                    "Subscription is expired. Registration is not allowed.");  // E03
        }
    }

    // ─── Scheduled — auto-expire subscriptions daily (BRULE_02, SUBS_05) ─────

    @Scheduled(cron = "0 0 0 * * *")  // runs every day at midnight
    @Transactional
    public void expireSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findByEndDateBeforeAndStatusNot(
                        LocalDate.now(),
                        MembershipStatus.CANCELLED
                );

        expiredSubscriptions.forEach(subscription -> {
            subscription.setStatus(MembershipStatus.EXPIRED);
            System.out.println("Auto-expired subscription id: " + subscription.getId());
        });

        subscriptionRepository.saveAll(expiredSubscriptions);
    }

    // ─── Helper — convert entity to DTO ──────────────────────────────────────

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getMember().getId(),
                subscription.getMembershipPlan().getName(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus()
        );
    }
}