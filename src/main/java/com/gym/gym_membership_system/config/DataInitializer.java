package com.gym.gym_membership_system.config;

import com.gym.gym_membership_system.domain.*;
import com.gym.gym_membership_system.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final MembershipPlanRepository membershipPlanRepository;

    public DataInitializer(RoleRepository roleRepository,
                           MembershipPlanRepository membershipPlanRepository) {
        this.roleRepository = roleRepository;
        this.membershipPlanRepository = membershipPlanRepository;
    }

    @Override
    public void run(String... args) {
        // Roles
        roleRepository.save(new Role(RoleName.ADMIN));
        roleRepository.save(new Role(RoleName.MEMBER));

        // Plans
        membershipPlanRepository.save(new MembershipPlan(
                MembershipType.BASIC, "Basic",
                "Access to gym floor only",
                30, 29.99, true));

        membershipPlanRepository.save(new MembershipPlan(
                MembershipType.PREMIUM, "Premium",
                "Full access including all classes",
                90, 69.99, true));

        System.out.println("=== Seed data loaded ===");
    }
}