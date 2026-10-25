package com.blooddonor.repository;

import com.blooddonor.model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findAllByOrderByCreatedAtDesc();

    List<BloodRequest> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);
}
