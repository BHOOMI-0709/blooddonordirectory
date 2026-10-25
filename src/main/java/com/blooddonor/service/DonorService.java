package com.blooddonor.service;

import com.blooddonor.model.Donor;
import com.blooddonor.model.PageResponse;
import com.blooddonor.model.Stats;
import com.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.repository.DonorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Business logic yahan: search, duplicate phone check, stats. Controller sirf request/response sambhalta hai. */
@Service
public class DonorService {

    private static final List<String> GROUPS = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    private final DonorRepository donors;
    private final BloodRequestRepository requests;

    public DonorService(DonorRepository donors, BloodRequestRepository requests) {
        this.donors = donors;
        this.requests = requests;
    }

    public PageResponse<Donor> search(String bloodGroup, String city, String q, Boolean available, int page, int size) {
        Specification<Donor> spec = (root, query, cb) -> cb.conjunction(); // shuru me "sab chalega"

        if (hasText(bloodGroup)) {
            // URL me '+' space ban jata hai ("A+" -> "A "), isliye wapas '+' kar rahe hain
            String bg = bloodGroup.replace(' ', '+').trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("bloodGroup"), bg));
        }
        if (hasText(city)) {
            String c = "%" + city.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("city")), c));
        }
        if (hasText(q)) {
            String n = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("fullName")), n));
        }
        if (available != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("available"), available));
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return PageResponse.of(donors.findAll(spec, pageable));
    }

    public Donor get(Long id) {
        return donors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
    }

    @Transactional
    public Donor create(Donor in) {
        if (donors.existsByPhone(in.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A donor with this phone number is already registered");
        }
        in.setId(null);
        in.setCreatedAt(null);
        return donors.save(in);
    }

    @Transactional
    public Donor update(Long id, Donor in) {
        Donor d = get(id);
        if (donors.existsByPhoneAndIdNot(in.getPhone(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This phone number belongs to another donor");
        }
        d.setFullName(in.getFullName());
        d.setBloodGroup(in.getBloodGroup());
        d.setAge(in.getAge());
        d.setGender(in.getGender());
        d.setPhone(in.getPhone());
        d.setEmail(in.getEmail());
        d.setCity(in.getCity());
        d.setState(in.getState());
        d.setLastDonationDate(in.getLastDonationDate());
        d.setAvailable(in.isAvailable());
        d.setNotes(in.getNotes());
        return donors.save(d);
    }

    @Transactional
    public void delete(Long id) {
        if (!donors.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found");
        }
        donors.deleteById(id);
    }

    public Stats stats() {
        Map<String, Long> byGroup = new LinkedHashMap<>();
        GROUPS.forEach(g -> byGroup.put(g, 0L)); // jin groups ka koi donor nahi unka count 0 dikhe
        for (Object[] row : donors.countByBloodGroup()) {
            byGroup.put((String) row[0], (Long) row[1]);
        }
        return new Stats(donors.count(), donors.countByAvailableTrue(), requests.countByStatus("Open"), byGroup);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
