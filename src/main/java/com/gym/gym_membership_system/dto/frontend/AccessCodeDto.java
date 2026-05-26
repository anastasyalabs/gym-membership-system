package com.gym.gym_membership_system.dto.frontend;

public record AccessCodeDto(
        String code,
        int remainingSeconds
) {
}
