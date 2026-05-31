package com.gym.gym_membership_system.dto;

import java.util.List;

public class CheckInResponse {

    private boolean success;
    private String message;
    private List<AttendanceRecordResponse> visits;

    public CheckInResponse() {
    }

    public CheckInResponse(boolean success, String message, List<AttendanceRecordResponse> visits) {
        this.success = success;
        this.message = message;
        this.visits = visits;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<AttendanceRecordResponse> getVisits() {
        return visits;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setVisits(List<AttendanceRecordResponse> visits) {
        this.visits = visits;
    }
}
