package com.gym.gym_membership_system.dto;

import com.gym.gym_membership_system.domain.MembershipType;

public class MembershipPlanRequest {

    private MembershipType membershipType;
    private String name;
    private String description;
    private int durationDays;
    private double price;

    public MembershipPlanRequest() {}

    public MembershipType getMembershipType() { return membershipType; }
    public void setMembershipType(MembershipType membershipType) { this.membershipType = membershipType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}