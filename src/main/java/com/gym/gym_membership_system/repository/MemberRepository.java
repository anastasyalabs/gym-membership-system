package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.gym.gym_membership_system.domain.UserAccount;
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserAccount(UserAccount userAccount);
}
