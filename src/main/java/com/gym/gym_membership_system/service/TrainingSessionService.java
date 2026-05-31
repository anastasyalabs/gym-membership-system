package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.RegistrationStatus;
import com.gym.gym_membership_system.domain.SessionStatus;
import com.gym.gym_membership_system.domain.TrainingSession;
import com.gym.gym_membership_system.dto.TrainingSessionRequest;
import com.gym.gym_membership_system.dto.TrainingSessionResponse;
import com.gym.gym_membership_system.exception.ResourceNotFoundException;
import com.gym.gym_membership_system.repository.SessionRegistrationRepository;
import com.gym.gym_membership_system.repository.TrainingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final SessionRegistrationRepository sessionRegistrationRepository;

    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository,
                                  SessionRegistrationRepository sessionRegistrationRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.sessionRegistrationRepository = sessionRegistrationRepository;
    }

    // ─── Get all sessions (visible to everyone) ───────────────────────────────

    public List<TrainingSessionResponse> getAllSessions() {
        return trainingSessionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Get only available sessions (for members) ────────────────────────────

    public List<TrainingSessionResponse> getAvailableSessions() {
        return trainingSessionRepository.findByStatus(SessionStatus.AVAILABLE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Get single session by id ─────────────────────────────────────────────

    public TrainingSessionResponse getSessionById(Long id) {
        TrainingSession session = trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training session not found with id: " + id));
        return toResponse(session);
    }

    // ─── Admin — create new session ───────────────────────────────────────────

    @Transactional
    public TrainingSessionResponse createSession(TrainingSessionRequest request) {
        // Validate dates
        if (request.getEndDatetime().isBefore(request.getStartDatetime())) {
            throw new IllegalArgumentException(
                    "End time cannot be before start time");
        }

        TrainingSession session = new TrainingSession(
                request.getTitle(),
                request.getTrainerName(),
                request.getStartDatetime(),
                request.getEndDatetime(),
                request.getCapacity(),
                SessionStatus.AVAILABLE  // always available when first created
        );
        trainingSessionRepository.save(session);
        return toResponse(session);
    }

    // ─── Admin — update existing session ─────────────────────────────────────

    @Transactional
    public TrainingSessionResponse updateSession(Long id, TrainingSessionRequest request) {
        TrainingSession session = trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training session not found with id: " + id));

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot update a cancelled session");
        }

        if (request.getTitle() != null)
            session.setTitle(request.getTitle());
        if (request.getTrainerName() != null)
            session.setTrainerName(request.getTrainerName());
        if (request.getStartDatetime() != null)
            session.setStartDatetime(request.getStartDatetime());
        if (request.getEndDatetime() != null)
            session.setEndDatetime(request.getEndDatetime());
        if (request.getCapacity() > 0)
            session.setCapacity(request.getCapacity());

        trainingSessionRepository.save(session);
        return toResponse(session);
    }

    // ─── Admin — cancel session (BRULE_07) ───────────────────────────────────

    @Transactional
    public void cancelSession(Long id) {
        TrainingSession session = trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training session not found with id: " + id));

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Session is already cancelled");
        }

        session.setStatus(SessionStatus.CANCELLED);
        trainingSessionRepository.save(session);
    }

    // ─── Helper used by SessionRegistrationService ────────────────────────────

    public TrainingSession getSessionEntityById(Long id) {
        return trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training session not found with id: " + id));
    }

    // ─── Called by SessionRegistrationService after every booking/cancel ─────

    @Transactional
    public void updateSessionStatus(TrainingSession session) {
        long activeRegistrations = sessionRegistrationRepository
                .countByTrainingSessionAndStatus(session, RegistrationStatus.REGISTERED);

        if (activeRegistrations >= session.getCapacity()) {
            session.setStatus(SessionStatus.FULL);
        } else {
            session.setStatus(SessionStatus.AVAILABLE);
        }
        trainingSessionRepository.save(session);
    }

    // ─── Helper — convert entity to DTO ──────────────────────────────────────

    private TrainingSessionResponse toResponse(TrainingSession session) {
        long registeredCount = sessionRegistrationRepository
                .countByTrainingSessionAndStatus(session, RegistrationStatus.REGISTERED);

        return new TrainingSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getTrainerName(),
                session.getStartDatetime(),
                session.getEndDatetime(),
                session.getCapacity(),
                (int) registeredCount,
                session.getStatus()
        );
    }
}