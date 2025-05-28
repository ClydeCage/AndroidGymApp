package com.example.hadonggymapp;

public class Amenity {
    private String id;
    private String name;

    // Required empty public constructor for Firestore
    public Amenity() {
    }

    public Amenity(String id, String name) {
        this.id = id;
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
} 