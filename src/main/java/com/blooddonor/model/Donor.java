package com.blooddonor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Donor table (donors). Har field ke upar validation rules hain,
 * galat data aaya to API 400 error degi.
 */
@Entity
@Table(name = "donors", uniqueConstraints = @UniqueConstraint(name = "uk_donor_phone", columnNames = "phone"))
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // client id set nahi kar sakta
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Please select a blood group")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood group (e.g. A+, O-)")
    @Column(nullable = false, length = 3)
    private String bloodGroup;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Donor must be at least 18 years old")
    @Max(value = 65, message = "Donor must be 65 years old or younger")
    @Column(nullable = false)
    private Integer age;

    @NotBlank(message = "Please select a gender")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female or Other")
    @Column(nullable = false, length = 10)
    private String gender;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    @Column(nullable = false, length = 10)
    private String phone;

    @Email(message = "Enter a valid email address")
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @NotBlank(message = "City is required")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String state;

    @PastOrPresent(message = "Last donation date cannot be in the future")
    private LocalDate lastDonationDate;

    private boolean available = true;

    @Size(max = 300, message = "Notes can be at most 300 characters")
    @Column(length = 300)
    private String notes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Blood donate karne ke baad rest period: men ~3 mahine, women ~4 mahine (general guideline).
     * Ye DB me save nahi hota, har baar calculate hota hai, aur JSON me "eligible" ban ke jata hai.
     */
    public boolean isEligible() {
        if (lastDonationDate == null) return true;
        long gapDays = "Female".equals(gender) ? 120 : 90;
        return ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now()) >= gapDays;
    }

    // ---------- getters & setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
