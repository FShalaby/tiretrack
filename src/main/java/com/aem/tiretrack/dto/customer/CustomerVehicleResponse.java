package com.aem.tiretrack.dto.customer;

import java.time.LocalDateTime;

import com.aem.tiretrack.model.CustomerVehicle;

public class CustomerVehicleResponse {
    private Long id;
    private String nickname;
    private String year;
    private String make;
    private String model;
    private String plateNumber;
    private String tireSize;
    private String tireSetup;
    private String frontTireSize;
    private String rearTireSize;
    private LocalDateTime createdAt;

    public CustomerVehicleResponse(CustomerVehicle vehicle) {
        this.id = vehicle.getId();
        this.nickname = vehicle.getNickname();
        this.year = vehicle.getYear();
        this.make = vehicle.getMake();
        this.model = vehicle.getModel();
        this.plateNumber = vehicle.getPlateNumber();
        this.tireSize = vehicle.getTireSize();
        this.tireSetup = vehicle.getTireSetup();
        this.frontTireSize = vehicle.getFrontTireSize();
        this.rearTireSize = vehicle.getRearTireSize();
        this.createdAt = vehicle.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getNickname() { return nickname; }
    public String getYear() { return year; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getPlateNumber() { return plateNumber; }
    public String getTireSize() { return tireSize; }
    public String getTireSetup() { return tireSetup; }
    public String getFrontTireSize() { return frontTireSize; }
    public String getRearTireSize() { return rearTireSize; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
