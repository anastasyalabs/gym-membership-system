package com.gym.gym_membership_system.config;

import com.gym.gym_membership_system.domain.Member;
import com.gym.gym_membership_system.domain.MembershipPlan;
import com.gym.gym_membership_system.domain.MembershipType;
import com.gym.gym_membership_system.domain.Role;
import com.gym.gym_membership_system.domain.RoleName;
import com.gym.gym_membership_system.domain.SessionStatus;
import com.gym.gym_membership_system.domain.TrainingSession;
import com.gym.gym_membership_system.domain.UserAccount;
import com.gym.gym_membership_system.repository.MemberRepository;
import com.gym.gym_membership_system.repository.MembershipPlanRepository;
import com.gym.gym_membership_system.repository.RoleRepository;
import com.gym.gym_membership_system.repository.TrainingSessionRepository;
import com.gym.gym_membership_system.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserAccountRepository userAccountRepository;
    private final MemberRepository memberRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    public DataInitializer(RoleRepository roleRepository,
                           MembershipPlanRepository membershipPlanRepository,
                           UserAccountRepository userAccountRepository,
                           MemberRepository memberRepository,
                           TrainingSessionRepository trainingSessionRepository) {
        this.roleRepository = roleRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.userAccountRepository = userAccountRepository;
        this.memberRepository = memberRepository;
        this.trainingSessionRepository = trainingSessionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = createRoleIfMissing(RoleName.ADMIN);
        Role memberRole = createRoleIfMissing(RoleName.MEMBER);

        createDefaultPlansIfMissing();
        createDefaultTrainingSessionsIfMissing();

        createMemberAccountIfMissing(
                "anna@gym.com",
                "anna123",
                "Anna",
                "Smith",
                "+371 22222222",
                LocalDate.of(1995, 3, 15),
                memberRole
        );

        createMemberAccountIfMissing(
                "admin@gym.com",
                "admin123",
                "Admin",
                "User",
                "+371 20000000",
                LocalDate.of(1990, 1, 1),
                adminRole
        );

        System.out.println("=== Seed data loaded: roles, plans, users, sessions ===");
    }

    private Role createRoleIfMissing(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private void createDefaultPlansIfMissing() {
        if (!membershipPlanRepository.findAll().isEmpty()) {
            return;
        }

        membershipPlanRepository.save(new MembershipPlan(
                MembershipType.BASIC,
                "Basic",
                "Access to gym floor only",
                30,
                29.99,
                true
        ));

        membershipPlanRepository.save(new MembershipPlan(
                MembershipType.PREMIUM,
                "Premium",
                "Full access including all classes",
                90,
                69.99,
                true
        ));
    }

    private void createDefaultTrainingSessionsIfMissing() {
        if (!trainingSessionRepository.findAll().isEmpty()) {
            return;
        }

        trainingSessionRepository.save(new TrainingSession(
                "Monday Yoga",
                "Laura",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 11, 0),
                10,
                SessionStatus.AVAILABLE
        ));

        trainingSessionRepository.save(new TrainingSession(
                "Strength Training",
                "Mark",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                LocalDateTime.of(2026, 6, 2, 19, 0),
                8,
                SessionStatus.AVAILABLE
        ));

        trainingSessionRepository.save(new TrainingSession(
                "Cardio Class",
                "Elena",
                LocalDateTime.of(2026, 6, 3, 17, 30),
                LocalDateTime.of(2026, 6, 3, 18, 30),
                12,
                SessionStatus.AVAILABLE
        ));
    }

    private void createMemberAccountIfMissing(String email,
                                              String password,
                                              String name,
                                              String surname,
                                              String phone,
                                              LocalDate dateOfBirth,
                                              Role role) {
        if (userAccountRepository.findByEmail(email).isPresent()) {
            return;
        }

        UserAccount account = new UserAccount(
                email,
                password,
                role
        );

        userAccountRepository.save(account);

        Member member = new Member(
                account,
                name,
                surname,
                phone,
                dateOfBirth
        );

        memberRepository.save(member);
    }
}