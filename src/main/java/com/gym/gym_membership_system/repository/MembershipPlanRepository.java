package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
}
