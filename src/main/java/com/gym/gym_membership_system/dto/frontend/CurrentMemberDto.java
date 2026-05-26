package com.gym.gym_membership_system.dto.frontend;

public record CurrentMemberDto(
        Long id,
        String firstName,
        String fullName,
        String email,
        String phone,
        String role,
        String status,
        String plan,
        String subscriptionEndDate
) {
}
