package com.gym.gym_membership_system.dto;

import com.gym.gym_membership_system.domain.SessionStatus;
import java.time.LocalDateTime;

public class TrainingSessionResponse {

    private Long id;
    private String title;
    private String trainerName;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private int capacity;
    private int registeredCount;   // how many have booked
    private int availablePlaces;   // capacity - registeredCount
    private SessionStatus status;

    public TrainingSessionResponse() {}

    public TrainingSessionResponse(Long id, String title, String trainerName,
                                   LocalDateTime startDatetime, LocalDateTime endDatetime,
                                   int capacity, int registeredCount, SessionStatus status) {
        this.id = id;
        this.title = title;
        this.trainerName = trainerName;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.capacity = capacity;
        this.registeredCount = registeredCount;
        this.availablePlaces = capacity - registeredCount;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public LocalDateTime getStartDatetime() { return startDatetime; }
    public void setStartDatetime(LocalDateTime startDatetime) { this.startDatetime = startDatetime; }

    public LocalDateTime getEndDatetime() { return endDatetime; }
    public void setEndDatetime(LocalDateTime endDatetime) { this.endDatetime = endDatetime; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
        this.availablePlaces = this.capacity - registeredCount;
    }

    public int getAvailablePlaces() { return availablePlaces; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
}