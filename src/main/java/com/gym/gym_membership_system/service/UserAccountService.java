package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.Member;
import com.gym.gym_membership_system.domain.Role;
import com.gym.gym_membership_system.domain.RoleName;
import com.gym.gym_membership_system.domain.UserAccount;
import com.gym.gym_membership_system.dto.RegisterRequest;
import com.gym.gym_membership_system.exception.ResourceNotFoundException;
import com.gym.gym_membership_system.repository.MemberRepository;
import com.gym.gym_membership_system.repository.RoleRepository;
import com.gym.gym_membership_system.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    public UserAccountService(UserAccountRepository userAccountRepository,
                              MemberRepository memberRepository,
                              RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public void register(RegisterRequest request) {
        // 1. Check email is not already taken
        if (userAccountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        // 2. Fetch the MEMBER role from DB
        Role memberRole = roleRepository.findByName(RoleName.MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MEMBER role not found — make sure DataInitializer has run"));

        // 3. Create UserAccount — password stored as plain text temporarily
        UserAccount account = new UserAccount(
                request.getEmail(),
                request.getPassword(),
                memberRole
        );
        userAccountRepository.save(account);

        // 4. Create Member profile linked to the account
        Member member = new Member(
                account,
                request.getName(),
                request.getSurname(),
                request.getPhone(),
                request.getDateOfBirth()
        );
        memberRepository.save(member);
    }

    // ─── Simple login check (no JWT for now) ──────────────────────────────────

    public UserAccount login(String email, String password) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + email));

        // Temporary plain text comparison — BCrypt will replace this later
        if (!account.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("Invalid password");
        }

        return account;
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    public UserAccount getByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + email));
    }
    @Transactional(readOnly = true)
    public String getRoleName(UserAccount account) {
        // Reload account within a transaction so Role can be lazily loaded
        UserAccount fresh = userAccountRepository.findById(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return fresh.getRole().getName().name();
    }
}