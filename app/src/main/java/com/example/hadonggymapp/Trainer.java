package com.example.hadonggymapp;

import java.io.Serializable;

public class Trainer implements Serializable {
    private String id;
    private String name;
    private String gymId;

    public Trainer() {}

    public Trainer(String id, String name, String gymId) {
        this.id = id;
        this.name = name;
        this.gymId = gymId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }
} 