package com.blooddonor.controller;

import com.blooddonor.config.AdminGuard;
import com.blooddonor.model.BloodRequest;
import com.blooddonor.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Blood request APIs.
 *   GET    /api/requests?status=Open|Fulfilled|All   (public)
 *   POST   /api/requests                             (public)
 *   PATCH  /api/requests/{id}/fulfil                 (admin)
 *   DELETE /api/requests/{id}                        (admin)
 */
@RestController
@RequestMapping("/api/requests")
public class BloodRequestController {

    private final BloodRequestService service;
    private final AdminGuard admin;

    public BloodRequestController(BloodRequestService service, AdminGuard admin) {
        this.service = service;
        this.admin = admin;
    }

    @GetMapping
    public List<BloodRequest> list(@RequestParam(required = false) String status) {
        return service.list(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloodRequest create(@Valid @RequestBody BloodRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/fulfil")
    public BloodRequest fulfil(@PathVariable Long id,
                               @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        admin.require(key);
        return service.markFulfilled(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        admin.require(key);
        service.delete(id);
    }
}
