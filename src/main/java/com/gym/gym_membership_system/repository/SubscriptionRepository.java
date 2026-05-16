package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.Subscription;
import com.gym.gym_membership_system.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByMember(Member member);
    java.util.List<Subscription> findByEndDateBeforeAndStatusNot(java.time.LocalDate date, com.gym.gym_membership_system.domain.MembershipStatus status);
}
