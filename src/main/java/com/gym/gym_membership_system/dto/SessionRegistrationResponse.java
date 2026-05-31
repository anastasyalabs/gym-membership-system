package com.gym.gym_membership_system.dto;

import com.gym.gym_membership_system.domain.RegistrationStatus;
import java.time.LocalDateTime;

public class SessionRegistrationResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long sessionId;
    private String sessionTitle;
    private RegistrationStatus status;
    private LocalDateTime createdAt;

    public SessionRegistrationResponse() {}

    public SessionRegistrationResponse(Long id, Long memberId, String memberName,
                                       Long sessionId, String sessionTitle,
                                       RegistrationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}