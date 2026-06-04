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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userAccountRepository,
                              MemberRepository memberRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userAccountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role memberRole = roleRepository.findByName(RoleName.MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MEMBER role not found — make sure DataInitializer has run"));

        UserAccount account = new UserAccount(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                memberRole
        );

        userAccountRepository.save(account);

        Member member = new Member(
                account,
                request.getName(),
                request.getSurname(),
                request.getPhone(),
                request.getDateOfBirth()
        );

        memberRepository.save(member);
    }

    @Transactional
    public UserAccount login(String email, String password) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + email));

        String storedPassword = account.getPasswordHash();

        if (storedPassword == null || storedPassword.isBlank()) {
            throw new IllegalArgumentException("Invalid password");
        }

        boolean passwordMatches;

        if (isBCryptHash(storedPassword)) {
            passwordMatches = passwordEncoder.matches(password, storedPassword);
        } else {
            // Temporary backward compatibility for old demo data stored as plain text.
            passwordMatches = storedPassword.equals(password);

            if (passwordMatches) {
                account.setPasswordHash(passwordEncoder.encode(password));
                userAccountRepository.save(account);
            }
        }

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid password");
        }

        return account;
    }

    public UserAccount getByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + email));
    }

    @Transactional(readOnly = true)
    public String getRoleName(UserAccount account) {
        UserAccount fresh = userAccountRepository.findById(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return fresh.getRole().getName().name();
    }

    private boolean isBCryptHash(String value) {
        return value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$");
    }
}