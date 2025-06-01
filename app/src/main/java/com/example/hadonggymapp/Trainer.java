package com.example.hadonggymapp;

import java.io.Serializable;

public class Trainer implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private String phone;
    private String specialization;
    private String description;
    private float rating;
    private int totalRatings;
    private String gymId; // ID của phòng tập mà trainer làm việc

    // Required empty public constructor for Firestore
    public Trainer() {
    }

    // Constructor with ID (useful when retrieving from Firestore)
    public Trainer(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor without ID (useful when creating a new Trainer before saving to Firestore)
    public Trainer(String name) {
        this.name = name;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getDescription() {
        return description;
    }

    public float getRating() {
        return rating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public String getGymId() {
        return gymId;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public void setGymId(String gymId) {
        this.gymId = gymId;
    }
} 