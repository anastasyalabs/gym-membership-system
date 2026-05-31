package com.gym.gym_membership_system.dto;

public class AttendanceRecordResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private String date;
    private String time;

    public AttendanceRecordResponse() {
    }

    public AttendanceRecordResponse(Long id, Long memberId, String memberName, String date, String time) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
