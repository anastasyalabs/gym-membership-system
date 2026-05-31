package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.Role;
import com.gym.gym_membership_system.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}