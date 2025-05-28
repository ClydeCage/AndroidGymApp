package com.example.hadonggymapp;

import java.io.Serializable;
import java.util.List; // Import List

public class Gym implements Serializable {
    private String name;
    private String address;
    private String phone;
    private String imageUrl; // Ảnh đại diện chính
    private String description; // Mô tả phòng gym
    private String hours; // Giờ mở cửa
    private List<String> services; // Các dịch vụ (Yoga, PT, Sauna...)
    private List<String> amenities; // Tiện ích (Wifi, gửi xe...)
    private List<String> imageUrls; // Danh sách các ảnh khác của phòng gym
    private Double latitude;
    private Double longitude;
    private String id;

    // Constructor rỗng - RẤT QUAN TRỌNG cho Firebase Firestore
    public Gym() {
    }

    // Constructor có tham số
    public Gym(String name, String address, String phone, String imageUrl, String description, String hours, List<String> services, List<String> amenities, List<String> imageUrls, Double latitude, Double longitude) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.description = description;
        this.hours = hours;
        this.services = services;
        this.amenities = amenities;
        this.imageUrls = imageUrls;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getHours() {
        return hours;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getId() {
        return id;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setId(String id) {
        this.id = id;
    }
}