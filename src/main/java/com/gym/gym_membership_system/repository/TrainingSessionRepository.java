package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
}
