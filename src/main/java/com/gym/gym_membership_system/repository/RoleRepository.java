package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
