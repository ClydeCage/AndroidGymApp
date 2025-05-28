package com.example.hadonggymapp;

import java.io.Serializable;

public class Trainer implements Serializable {
    private String id;
    private String name;
    // Có thể thêm các trường khác như imageUrl, phone, specialization, description, etc.

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

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Add setters for other fields if added
    // public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    // public void setPhone(String phone) { this.phone = phone; }
} 