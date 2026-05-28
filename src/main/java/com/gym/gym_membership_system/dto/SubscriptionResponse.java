package com.gym.gym_membership_system.dto;

import com.gym.gym_membership_system.domain.MembershipStatus;
import java.time.LocalDate;

public class SubscriptionResponse {

    private Long id;
    private Long memberId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private MembershipStatus status;

    public SubscriptionResponse() {}

    public SubscriptionResponse(Long id, Long memberId, String planName,
                                LocalDate startDate, LocalDate endDate,
                                MembershipStatus status) {
        this.id = id;
        this.memberId = memberId;
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }
}