package com.gym.gym_membership_system.dto;

import com.gym.gym_membership_system.domain.MembershipType;

public class MembershipPlanResponse {

    private Long id;
    private MembershipType membershipType;
    private String name;
    private String description;
    private int durationDays;
    private double price;
    private boolean active;

    public MembershipPlanResponse() {}

    public MembershipPlanResponse(Long id, MembershipType membershipType, String name,
                                  String description, int durationDays,
                                  double price, boolean active) {
        this.id = id;
        this.membershipType = membershipType;
        this.name = name;
        this.description = description;
        this.durationDays = durationDays;
        this.price = price;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}