package com.gym.gym_membership_system.controller;

import com.gym.gym_membership_system.domain.UserAccount;
import com.gym.gym_membership_system.dto.AuthResponse;
import com.gym.gym_membership_system.dto.LoginRequest;
import com.gym.gym_membership_system.dto.RegisterRequest;
import com.gym.gym_membership_system.service.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    // ─── Register ─────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userAccountService.register(request);
        return ResponseEntity.ok("Account created successfully");
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        UserAccount account = userAccountService.login(
                request.getEmail(),
                request.getPassword()
        );

        // Fix — get role name inside UserAccountService, not here
        String roleName = userAccountService.getRoleName(account);

        AuthResponse response = new AuthResponse(
                "temp-token-" + account.getId(),
                roleName,
                account.getId(),
                account.getEmail()
        );
        return ResponseEntity.ok(response);
    }
}