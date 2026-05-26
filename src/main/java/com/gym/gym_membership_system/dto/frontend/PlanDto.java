package com.gym.gym_membership_system.dto.frontend;

import java.util.List;

public record PlanDto(
        String name,
        double price,
        String duration,
        String description,
        List<String> features,
        boolean popular
) {
}
