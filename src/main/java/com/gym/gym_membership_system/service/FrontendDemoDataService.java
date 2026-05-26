package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.dto.frontend.AccessCodeDto;
import com.gym.gym_membership_system.dto.frontend.CheckInResponse;
import com.gym.gym_membership_system.dto.frontend.CurrentMemberDto;
import com.gym.gym_membership_system.dto.frontend.PlanDto;
import com.gym.gym_membership_system.dto.frontend.TrainingSessionDto;
import com.gym.gym_membership_system.dto.frontend.VisitRecordDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FrontendDemoDataService {

    private String currentPlanName = "Premium";
    private String currentStatus = "ACTIVE";

    private final List<PlanDto> plans = List.of(
            new PlanDto(
                    "Basic",
                    25,
                    "30 days",
                    "Gym access during standard opening hours.",
                    List.of("Gym equipment", "Locker room", "Standard access"),
                    false
            ),
            new PlanDto(
                    "Premium",
                    45,
                    "30 days",
                    "Full access with group classes and priority booking.",
                    List.of("Gym equipment", "Group classes", "Priority booking"),
                    true
            ),
            new PlanDto(
                    "Student",
                    18,
                    "30 days",
                    "Discounted plan for students with valid student ID.",
                    List.of("Gym equipment", "Student discount", "Standard access"),
                    false
            )
    );

    private final List<TrainingSessionDto> sessions = new ArrayList<>(List.of(
            new TrainingSessionDto(1L, "Morning Yoga", "Laura", "2026-05-28", "08:00", 12, 8, false),
            new TrainingSessionDto(2L, "Strength Training", "Mark", "2026-05-28", "18:00", 10, 10, false),
            new TrainingSessionDto(3L, "Cardio Class", "Elena", "2026-05-29", "17:30", 15, 4, false),
            new TrainingSessionDto(4L, "Pilates Core", "Sofia", "2026-05-30", "19:00", 14, 9, false)
    ));

    private final List<VisitRecordDto> visits = new ArrayList<>(List.of(
            new VisitRecordDto("2026-05-01", "18:20"),
            new VisitRecordDto("2026-05-03", "09:45"),
            new VisitRecordDto("2026-05-05", "17:35")
    ));

    public CurrentMemberDto getCurrentMember() {
        return new CurrentMemberDto(
                1L,
                "Anna",
                "Anna Smith",
                "anna.smith@example.com",
                "+371 20000001",
                "MEMBER",
                currentStatus,
                currentPlanName,
                "2026-05-31"
        );
    }

    public List<PlanDto> getPlans() {
        return plans;
    }

    public List<TrainingSessionDto> getSessions() {
        return sessions;
    }

    public List<VisitRecordDto> getVisits() {
        return visits.stream()
                .sorted(Comparator.comparing(VisitRecordDto::date).thenComparing(VisitRecordDto::time))
                .toList();
    }

    public CurrentMemberDto changePlan(String planName) {
        boolean planExists = plans.stream()
                .anyMatch(plan -> plan.name().equalsIgnoreCase(planName));

        if (!planExists) {
            throw new IllegalArgumentException("Selected plan does not exist.");
        }

        currentPlanName = plans.stream()
                .filter(plan -> plan.name().equalsIgnoreCase(planName))
                .findFirst()
                .orElseThrow()
                .name();

        currentStatus = "ACTIVE";

        return getCurrentMember();
    }

    public TrainingSessionDto registerForSession(Long sessionId) {
        for (int i = 0; i < sessions.size(); i++) {
            TrainingSessionDto session = sessions.get(i);

            if (!session.id().equals(sessionId)) {
                continue;
            }

            if (!"ACTIVE".equals(currentStatus)) {
                throw new IllegalStateException("Subscription is not active.");
            }

            if (session.booked() >= session.capacity()) {
                throw new IllegalStateException("Training session is full.");
            }

            if (session.registered()) {
                return session;
            }

            TrainingSessionDto updatedSession = new TrainingSessionDto(
                    session.id(),
                    session.title(),
                    session.trainer(),
                    session.date(),
                    session.time(),
                    session.capacity(),
                    session.booked() + 1,
                    true
            );

            sessions.set(i, updatedSession);
            return updatedSession;
        }

        throw new IllegalArgumentException("Training session was not found.");
    }

    public AccessCodeDto getCurrentAccessCode() {
        return new AccessCodeDto(generateCurrentAccessCode(), getRemainingCodeSeconds());
    }

    public CheckInResponse checkIn(String enteredCode) {
        if (!"ACTIVE".equals(currentStatus)) {
            return new CheckInResponse(false, "Your subscription is not active. Check-in is not allowed.", getVisits());
        }

        if (enteredCode == null || !generateCurrentAccessCode().equals(enteredCode.trim())) {
            return new CheckInResponse(false, "Invalid access code. Please ask gym staff for the current code.", getVisits());
        }

        LocalDate today = LocalDate.now();
        boolean alreadyCheckedInToday = visits.stream()
                .anyMatch(visit -> visit.date().equals(today.toString()));

        if (alreadyCheckedInToday) {
            return new CheckInResponse(true, "You have already checked in today.", getVisits());
        }

        VisitRecordDto visit = new VisitRecordDto(
                today.toString(),
                LocalTime.now().withSecond(0).withNano(0).toString()
        );

        visits.add(visit);

        return new CheckInResponse(true, "Check-in successful. Welcome to the gym!", getVisits());
    }

    private String generateCurrentAccessCode() {
        long minuteSeed = Instant.now().getEpochSecond() / 60;
        long value = Math.abs((minuteSeed * 9301 + 49297) % 1000000);

        return String.format("%06d", value);
    }

    private int getRemainingCodeSeconds() {
        return 60 - LocalTime.now().getSecond();
    }
}
