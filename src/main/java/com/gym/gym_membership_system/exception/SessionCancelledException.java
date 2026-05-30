package com.gym.gym_membership_system.exception;

public class SessionCancelledException extends RuntimeException {
    public SessionCancelledException(String message) {
        super(message);
    }
}