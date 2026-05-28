package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.Member;
import com.gym.gym_membership_system.domain.RegistrationStatus;
import com.gym.gym_membership_system.domain.SessionRegistration;
import com.gym.gym_membership_system.domain.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRegistrationRepository extends JpaRepository<SessionRegistration, Long> {
    List<SessionRegistration> findByTrainingSession(TrainingSession trainingSession);
    List<SessionRegistration> findByMember(Member member);
    long countByTrainingSessionAndStatus(TrainingSession trainingSession, RegistrationStatus status);
}