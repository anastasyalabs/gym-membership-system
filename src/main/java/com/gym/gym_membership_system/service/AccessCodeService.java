package com.gym.gym_membership_system.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;

@Service
public class AccessCodeService {

    public String getCurrentCode() {
        long minuteSeed = Instant.now().getEpochSecond() / 60;
        long rawValue = Math.abs((minuteSeed * 9301 + 49297) % 1000000);

        return String.format("%06d", rawValue);
    }

    public int getRemainingSeconds() {
        return 60 - LocalTime.now().getSecond();
    }

    public boolean isValidCode(String enteredCode) {
        if (enteredCode == null || enteredCode.isBlank()) {
            return false;
        }

        return getCurrentCode().equals(enteredCode.trim());
    }
}
