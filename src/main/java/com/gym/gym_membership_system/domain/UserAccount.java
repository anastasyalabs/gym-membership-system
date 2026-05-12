package com.gym.gym_membership_system.domain;

public class UserAccount {
    private String email;
    private String password;
    private boolean active = false;
    public UserAccount(String email, String password){
    this.email = email;
    this.password = password;
    }

}
