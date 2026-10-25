package com.blooddonor.service;

import com.blooddonor.model.BloodRequest;
import com.blooddonor.repository.BloodRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BloodRequestService {

    private final BloodRequestRepository repo;

    public BloodRequestService(BloodRequestRepository repo) {
        this.repo = repo;
    }

    public List<BloodRequest> list(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("All")) {
            return repo.findAllByOrderByCreatedAtDesc();
        }
        return repo.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional
    public BloodRequest create(BloodRequest in) {
        in.setId(null);
        in.setStatus("Open");
        in.setCreatedAt(null);
        return repo.save(in);
    }

    @Transactional
    public BloodRequest markFulfilled(Long id) {
        BloodRequest r = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        r.setStatus("Fulfilled");
        return repo.save(r);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
        }
        repo.deleteById(id);
    }
}
