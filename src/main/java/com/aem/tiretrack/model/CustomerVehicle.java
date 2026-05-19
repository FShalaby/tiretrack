package com.aem.tiretrack.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_vehicles")
public class CustomerVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    private String nickname;
    private String year;
    private String make;
    private String model;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "tire_size")
    private String tireSize;

    @Column(name = "tire_setup")
    private String tireSetup = "regular";

    @Column(name = "front_tire_size")
    private String frontTireSize;

    @Column(name = "rear_tire_size")
    private String rearTireSize;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public String getTireSize() { return tireSize; }
    public void setTireSize(String tireSize) { this.tireSize = tireSize; }
    public String getTireSetup() { return tireSetup; }
    public void setTireSetup(String tireSetup) { this.tireSetup = tireSetup; }
    public String getFrontTireSize() { return frontTireSize; }
    public void setFrontTireSize(String frontTireSize) { this.frontTireSize = frontTireSize; }
    public String getRearTireSize() { return rearTireSize; }
    public void setRearTireSize(String rearTireSize) { this.rearTireSize = rearTireSize; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
