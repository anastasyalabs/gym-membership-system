package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
