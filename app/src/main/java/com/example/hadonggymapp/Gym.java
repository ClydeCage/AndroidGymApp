package com.example.hadonggymapp;

import java.io.Serializable;

public class Gym implements Serializable {
    private String name;
    private String address;
    private String phone;
    private String imageUrl; // THAY ĐỔI: từ int imageResourceId sang String imageUrl
    private String description; // THÊM MỚI: nếu bạn có trường này trên Firestore

    // Constructor rỗng - RẤT QUAN TRỌNG cho Firebase Firestore
    public Gym() {
    }

    // Constructor có tham số - Cập nhật để dùng imageUrl và description
    public Gym(String name, String address, String phone, String imageUrl, String description) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getImageUrl() { // THAY ĐỔI
        return imageUrl;
    }

    public String getDescription() { // THÊM MỚI
        return description;
    }

    // Setters - Firebase cũng cần setters để gán giá trị
    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setImageUrl(String imageUrl) { // THAY ĐỔI
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) { // THÊM MỚI
        this.description = description;
    }
}