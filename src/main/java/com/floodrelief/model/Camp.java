package com.floodrelief.model;

import java.time.LocalDateTime;

/**
 * Relief Camp in flood-affected Haor regions.
 */
public class Camp {
    private String campId;
    private String name;
    private String district; // Sylhet, Sunamganj, Moulvibazar, Habiganj
    private String area;     // Companiganj, Gowainghat, Tahirpur, Derai, etc.
    private int population;
    private int capacity;
    private int waterloggedDays;
    private int medicalNeed; // count of acute sick/injured/diarrhea patients
    private LocalDateTime lastReliefDate;
    private double severityScore;
    private SeverityLabel severityLabel;
    private double latitude;
    private double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Camp() {
        this.severityLabel = SeverityLabel.LOW;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Camp(String campId, String name, String district, String area, int population, int capacity,
                int waterloggedDays, int medicalNeed, LocalDateTime lastReliefDate) {
        this.campId = campId;
        this.name = name;
        this.district = district;
        this.area = area;
        this.population = population;
        this.capacity = capacity;
        this.waterloggedDays = waterloggedDays;
        this.medicalNeed = medicalNeed;
        this.lastReliefDate = lastReliefDate;
        this.severityLabel = SeverityLabel.LOW;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getCampId() { return campId; }
    public void setCampId(String campId) { this.campId = campId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public int getPopulation() { return population; }
    public void setPopulation(int population) { this.population = population; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getWaterloggedDays() { return waterloggedDays; }
    public void setWaterloggedDays(int waterloggedDays) { this.waterloggedDays = waterloggedDays; }

    public int getMedicalNeed() { return medicalNeed; }
    public void setMedicalNeed(int medicalNeed) { this.medicalNeed = medicalNeed; }

    public LocalDateTime getLastReliefDate() { return lastReliefDate; }
    public void setLastReliefDate(LocalDateTime lastReliefDate) { this.lastReliefDate = lastReliefDate; }

    public double getSeverityScore() { return severityScore; }
    public void setSeverityScore(double severityScore) {
        this.severityScore = severityScore;
        this.severityLabel = SeverityLabel.fromScore(severityScore);
    }

    public SeverityLabel getSeverityLabel() { return severityLabel; }
    public void setSeverityLabel(SeverityLabel severityLabel) { this.severityLabel = severityLabel; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getOccupancyRate() {
        if (capacity <= 0) return 0.0;
        return ((double) population / capacity) * 100.0;
    }

    @Override
    public String toString() {
        return name + " (" + area + ", " + district + ")";
    }
}
