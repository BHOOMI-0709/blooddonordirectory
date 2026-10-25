package com.blooddonor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Kisi patient ko blood chahiye to ye request yahan save hoti hai (blood_requests table).
 */
@Entity
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Patient name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String patientName;

    @NotBlank(message = "Please select a blood group")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood group")
    @Column(nullable = false, length = 3)
    private String bloodGroup;

    @NotNull(message = "Units are required")
    @Min(value = 1, message = "At least 1 unit is required")
    @Max(value = 10, message = "Maximum 10 units per request")
    @Column(nullable = false)
    private Integer units;

    @NotBlank(message = "Hospital name is required")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String hospital;

    @NotBlank(message = "City is required")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String city;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    @Column(nullable = false, length = 10)
    private String contactPhone;

    @NotBlank(message = "Please select an urgency level")
    @Pattern(regexp = "^(Normal|Urgent|Critical)$", message = "Urgency must be Normal, Urgent or Critical")
    @Column(nullable = false, length = 10)
    private String urgency;

    @NotNull(message = "Please enter the date by which blood is needed")
    @FutureOrPresent(message = "Date must be today or later")
    @Column(nullable = false)
    private LocalDate requiredDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, length = 10)
    private String status = "Open"; // Open / Fulfilled

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "Open";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Integer getUnits() { return units; }
    public void setUnits(Integer units) { this.units = units; }

    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public LocalDate getRequiredDate() { return requiredDate; }
    public void setRequiredDate(LocalDate requiredDate) { this.requiredDate = requiredDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
