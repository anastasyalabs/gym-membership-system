package com.gym.gym_membership_system.dto;

public class AccessCodeResponse {

    private String code;
    private int remainingSeconds;

    public AccessCodeResponse() {
    }

    public AccessCodeResponse(String code, int remainingSeconds) {
        this.code = code;
        this.remainingSeconds = remainingSeconds;
    }

    public String getCode() {
        return code;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
