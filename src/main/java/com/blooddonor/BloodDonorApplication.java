package com.blooddonor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Yahin se poora app start hota hai.
 * @SpringBootApplication = config + component scan + auto-configuration, teeno ek saath.
 */
@SpringBootApplication
public class BloodDonorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodDonorApplication.class, args);
    }
}
