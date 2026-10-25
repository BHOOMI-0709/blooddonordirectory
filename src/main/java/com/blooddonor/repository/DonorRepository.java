package com.blooddonor.repository;

import com.blooddonor.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * JpaRepository = save/findById/delete jaise methods ready-made.
 * JpaSpecificationExecutor = dynamic filters (blood group, city, name...) ke liye.
 */
public interface DonorRepository extends JpaRepository<Donor, Long>, JpaSpecificationExecutor<Donor> {

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    long countByAvailableTrue();

    @Query("select d.bloodGroup, count(d) from Donor d group by d.bloodGroup")
    List<Object[]> countByBloodGroup();
}
