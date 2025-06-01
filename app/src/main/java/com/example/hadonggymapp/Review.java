package com.example.hadonggymapp;

public class Review {
    private String id;
    private String userId;
    private String userName;
    private String gymId;
    private float rating;
    private String comment;
    private long timestamp;

    public Review() {}

    public Review(String id, String userId, String userName, String gymId, float rating, String comment, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.gymId = gymId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
} 