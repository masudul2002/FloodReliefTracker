package com.floodrelief.model;

import java.time.LocalDateTime;

/**
 * Beneficiary family registered within a flood relief camp.
 */
public class Family {
    private String familyId;
    private String campId;
    private String campName;
    private String headName;
    private int memberCount;
    private String vulnerabilityNotes; // Elderly, infants, pregnant, disabled members
    private LocalDateTime registrationDate;

    public Family() {
        this.memberCount = 1;
        this.registrationDate = LocalDateTime.now();
    }

    public Family(String familyId, String campId, String headName, int memberCount, String vulnerabilityNotes) {
        this.familyId = familyId;
        this.campId = campId;
        this.headName = headName;
        this.memberCount = memberCount;
        this.vulnerabilityNotes = vulnerabilityNotes;
        this.registrationDate = LocalDateTime.now();
    }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getCampId() { return campId; }
    public void setCampId(String campId) { this.campId = campId; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public String getHeadName() { return headName; }
    public void setHeadName(String headName) { this.headName = headName; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getVulnerabilityNotes() { return vulnerabilityNotes; }
    public void setVulnerabilityNotes(String vulnerabilityNotes) { this.vulnerabilityNotes = vulnerabilityNotes; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    @Override
    public String toString() {
        return headName + " (ID: " + familyId + ", " + memberCount + " members)";
    }
}
