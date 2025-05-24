package com.example.hadonggymapp;

import java.util.Date;

public class WorkoutSchedule {
    private String id;
    private String userId;
    private String gymId;
    private String trainerId;
    private Date workoutDate;
    private String workoutTime;
    private String status; // "pending", "confirmed", "completed", "cancelled"
    private String notes;
    private int rating;
    private String review;

    // Empty constructor for Firestore
    public WorkoutSchedule() {}

    public WorkoutSchedule(String id, String userId, String gymId, String trainerId, 
                         Date workoutDate, String workoutTime, String status) {
        this.id = id;
        this.userId = userId;
        this.gymId = gymId;
        this.trainerId = trainerId;
        this.workoutDate = workoutDate;
        this.workoutTime = workoutTime;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }

    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }

    public Date getWorkoutDate() { return workoutDate; }
    public void setWorkoutDate(Date workoutDate) { this.workoutDate = workoutDate; }

    public String getWorkoutTime() { return workoutTime; }
    public void setWorkoutTime(String workoutTime) { this.workoutTime = workoutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
} 