package com.blooddonor.controller;

import com.blooddonor.config.AdminGuard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Frontend ka "Admin Login" button isi se key verify karta hai. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminGuard admin;

    public AdminController(AdminGuard admin) {
        this.admin = admin;
    }

    @GetMapping("/verify")
    public Map<String, Boolean> verify(@RequestHeader(value = "X-Admin-Key", required = false) String key) {
        admin.require(key); // galat ho to 401
        return Map.of("ok", true);
    }
}
