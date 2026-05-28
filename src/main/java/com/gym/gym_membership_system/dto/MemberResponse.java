package com.gym.gym_membership_system.dto;

import java.time.LocalDate;

public class MemberResponse {

    private Long id;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private LocalDate dateOfBirth;
    private String status;

    public MemberResponse() {}

    public MemberResponse(Long id, String email, String name, String surname,
                          String phone, LocalDate dateOfBirth, String status) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}