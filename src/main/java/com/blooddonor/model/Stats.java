package com.blooddonor.model;

import java.util.Map;

/** Home page ke counters ke liye. */
public record Stats(long totalDonors, long availableDonors, long openRequests, Map<String, Long> byBloodGroup) {
}
