package com.blooddonor.controller;

import com.blooddonor.config.AdminGuard;
import com.blooddonor.model.Donor;
import com.blooddonor.model.PageResponse;
import com.blooddonor.model.Stats;
import com.blooddonor.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Donor APIs.
 *   GET    /api/donors            -> search + filter + pagination   (public)
 *   GET    /api/donors/stats      -> counters                        (public)
 *   GET    /api/donors/{id}       -> ek donor                        (public)
 *   POST   /api/donors            -> naya donor register             (public)
 *   PUT    /api/donors/{id}       -> edit                            (admin)
 *   DELETE /api/donors/{id}       -> delete                          (admin)
 */
@RestController
@RequestMapping("/api/donors")
public class DonorController {

    private final DonorService service;
    private final AdminGuard admin;

    public DonorController(DonorService service, AdminGuard admin) {
        this.service = service;
        this.admin = admin;
    }

    @GetMapping
    public PageResponse<Donor> search(@RequestParam(required = false) String bloodGroup,
                                      @RequestParam(required = false) String city,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(required = false) Boolean available,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "9") int size) {
        return service.search(bloodGroup, city, q, available, page, size);
    }

    @GetMapping("/stats")
    public Stats stats() {
        return service.stats();
    }

    @GetMapping("/{id}")
    public Donor get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Donor create(@Valid @RequestBody Donor donor) {
        return service.create(donor);
    }

    @PutMapping("/{id}")
    public Donor update(@PathVariable Long id,
                        @Valid @RequestBody Donor donor,
                        @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        admin.require(key);
        return service.update(id, donor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        admin.require(key);
        service.delete(id);
    }
}
