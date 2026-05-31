package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.SessionStatus;
import com.gym.gym_membership_system.domain.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByStatus(SessionStatus status);   // ← add this
}