package com.gym.gym_membership_system.dto;

public class AuthResponse {

    private String token;
    private String role;
    private Long memberId;
    private String email;

    public AuthResponse(String token, String role, Long memberId, String email) {
        this.token = token;
        this.role = role;
        this.memberId = memberId;
        this.email = email;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}