package com.gym.gym_membership_system.exception;

public class SubscriptionNotActiveException extends RuntimeException {
    public SubscriptionNotActiveException(String message) {
        super(message);
    }
}