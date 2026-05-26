package com.gym.gym_membership_system.dto.frontend;

import java.util.List;

public record CheckInResponse(
        boolean success,
        String message,
        List<VisitRecordDto> visits
) {
}
