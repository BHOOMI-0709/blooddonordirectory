package com.blooddonor.config;

import com.blooddonor.model.BloodRequest;
import com.blooddonor.model.Donor;
import com.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Demo data: sirf tab chalta hai jab app.seed-demo-data=true ho aur table khaali ho.
 * Ye sab FAKE data hai (9000000xxx numbers) - production me SEED_DEMO=false rakho.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final DonorRepository donors;
    private final BloodRequestRepository requests;
    private final boolean enabled;

    public DataSeeder(DonorRepository donors, BloodRequestRepository requests,
                      @Value("${app.seed-demo-data:false}") boolean enabled) {
        this.donors = donors;
        this.requests = requests;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || donors.count() > 0) return;

        LocalDate today = LocalDate.now();
        donors.save(donor("Aarav Sharma", "B+", 28, "Male", "9000000001", "Lucknow", "Uttar Pradesh", today.minusDays(200), true));
        donors.save(donor("Priya Verma", "O+", 25, "Female", "9000000002", "Meerut", "Uttar Pradesh", null, true));
        donors.save(donor("Rohit Singh", "A+", 32, "Male", "9000000003", "Delhi", "Delhi", today.minusDays(40), true));
        donors.save(donor("Neha Gupta", "AB-", 29, "Female", "9000000004", "Noida", "Uttar Pradesh", today.minusDays(300), true));
        donors.save(donor("Imran Khan", "O-", 35, "Male", "9000000005", "Lucknow", "Uttar Pradesh", today.minusDays(120), true));
        donors.save(donor("Sneha Patel", "B-", 27, "Female", "9000000006", "Ahmedabad", "Gujarat", today.minusDays(10), false));
        donors.save(donor("Karan Mehta", "A-", 41, "Male", "9000000007", "Mumbai", "Maharashtra", null, true));
        donors.save(donor("Anjali Yadav", "AB+", 23, "Female", "9000000008", "Meerut", "Uttar Pradesh", today.minusDays(150), true));

        requests.save(request("Ramesh Kumar", "O-", 2, "District Hospital", "Meerut", "9000000011", "Urgent", today.plusDays(1)));
        requests.save(request("Sunita Devi", "B+", 1, "City Care Hospital", "Lucknow", "9000000012", "Normal", today.plusDays(4)));
    }

    private Donor donor(String name, String bg, int age, String gender, String phone,
                        String city, String state, LocalDate last, boolean available) {
        Donor d = new Donor();
        d.setFullName(name);
        d.setBloodGroup(bg);
        d.setAge(age);
        d.setGender(gender);
        d.setPhone(phone);
        d.setCity(city);
        d.setState(state);
        d.setLastDonationDate(last);
        d.setAvailable(available);
        return d;
    }

    private BloodRequest request(String patient, String bg, int units, String hospital,
                                 String city, String phone, String urgency, LocalDate date) {
        BloodRequest r = new BloodRequest();
        r.setPatientName(patient);
        r.setBloodGroup(bg);
        r.setUnits(units);
        r.setHospital(hospital);
        r.setCity(city);
        r.setContactPhone(phone);
        r.setUrgency(urgency);
        r.setRequiredDate(date);
        return r;
    }
}
