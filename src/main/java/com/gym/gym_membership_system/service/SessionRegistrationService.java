package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.Member;
import com.gym.gym_membership_system.domain.RegistrationStatus;
import com.gym.gym_membership_system.domain.SessionRegistration;
import com.gym.gym_membership_system.domain.SessionStatus;
import com.gym.gym_membership_system.domain.TrainingSession;
import com.gym.gym_membership_system.dto.SessionRegistrationResponse;
import com.gym.gym_membership_system.exception.ResourceNotFoundException;
import com.gym.gym_membership_system.exception.SessionCancelledException;
import com.gym.gym_membership_system.exception.SessionFullException;
import com.gym.gym_membership_system.repository.SessionRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionRegistrationService {

    private final SessionRegistrationRepository sessionRegistrationRepository;
    private final MemberService memberService;
    private final TrainingSessionService trainingSessionService;
    private final SubscriptionService subscriptionService;

    public SessionRegistrationService(
            SessionRegistrationRepository sessionRegistrationRepository,
            MemberService memberService,
            TrainingSessionService trainingSessionService,
            SubscriptionService subscriptionService) {
        this.sessionRegistrationRepository = sessionRegistrationRepository;
        this.memberService = memberService;
        this.trainingSessionService = trainingSessionService;
        this.subscriptionService = subscriptionService;
    }

    // ─── Member — register for a session ─────────────────────────────────────

    @Transactional
    public SessionRegistrationResponse registerForSession(String email, Long sessionId) {
        // 1. Get the member
        Member member = memberService.getMemberEntityByEmail(email);

        // 2. Get the session
        TrainingSession session = trainingSessionService.getSessionEntityById(sessionId);

        // 3. BRULE_07 — session must not be cancelled
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new SessionCancelledException(
                    "Cannot register for a cancelled session");
        }

        // 4. BRULE_04 — session must not be full
        long activeRegistrations = sessionRegistrationRepository
                .countByTrainingSessionAndStatus(session, RegistrationStatus.REGISTERED);
        if (activeRegistrations >= session.getCapacity()) {
            throw new SessionFullException(
                    "Training session is full");  // E05
        }

        // 5. BRULE_01, 02, 03 — member must have active subscription
        subscriptionService.checkMemberHasActiveSubscription(member);

        // 6. Check member is not already registered for this session
        List<SessionRegistration> existing = sessionRegistrationRepository
                .findByMember(member);
        boolean alreadyRegistered = existing.stream()
                .anyMatch(r -> r.getTrainingSession().getId().equals(sessionId)
                        && r.getStatus() == RegistrationStatus.REGISTERED);
        if (alreadyRegistered) {
            throw new IllegalArgumentException(
                    "You are already registered for this session");
        }

        // 7. All checks passed — create the registration
        SessionRegistration registration = new SessionRegistration(
                session,
                member,
                RegistrationStatus.REGISTERED,
                null  // createdAt is set automatically by @PrePersist
        );
        sessionRegistrationRepository.save(registration);

        // 8. Update session status (might become FULL now)
        trainingSessionService.updateSessionStatus(session);

        return toResponse(registration);
    }

    // ─── Member — cancel their own registration ───────────────────────────────

    @Transactional
    public void cancelRegistration(String email, Long registrationId) {
        // 1. Find the registration
        SessionRegistration registration = sessionRegistrationRepository
                .findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found with id: " + registrationId));

        // 2. Make sure it belongs to the logged-in member
        Member member = memberService.getMemberEntityByEmail(email);
        if (!registration.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException(
                    "You can only cancel your own registrations");
        }

        // 3. Check it is not already cancelled
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Registration is already cancelled");
        }

        // 4. Cancel it
        registration.setStatus(RegistrationStatus.CANCELLED);
        sessionRegistrationRepository.save(registration);

        // 5. Update session status (might become AVAILABLE again)
        trainingSessionService.updateSessionStatus(registration.getTrainingSession());
    }

    // ─── Admin — view all registrations for a session ────────────────────────

    public List<SessionRegistrationResponse> getRegistrationsForSession(Long sessionId) {
        TrainingSession session = trainingSessionService.getSessionEntityById(sessionId);
        return sessionRegistrationRepository.findByTrainingSession(session)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Member — view own registrations ─────────────────────────────────────

    public List<SessionRegistrationResponse> getMyRegistrations(String email) {
        Member member = memberService.getMemberEntityByEmail(email);
        return sessionRegistrationRepository.findByMember(member)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Helper — convert entity to DTO ──────────────────────────────────────

    private SessionRegistrationResponse toResponse(SessionRegistration registration) {
        return new SessionRegistrationResponse(
                registration.getId(),
                registration.getMember().getId(),
                registration.getMember().getName() + " " + registration.getMember().getSurname(),
                registration.getTrainingSession().getId(),
                registration.getTrainingSession().getTitle(),
                registration.getStatus(),
                registration.getCreatedAt()
        );
    }
}