package com.gym.gym_membership_system.dto.frontend;

public record TrainingSessionDto(
        Long id,
        String title,
        String trainer,
        String date,
        String time,
        int capacity,
        int booked,
        boolean registered
) {
}
