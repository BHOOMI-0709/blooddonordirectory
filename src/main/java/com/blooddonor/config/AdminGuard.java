package com.blooddonor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Simple admin protection: request header "X-Admin-Key" config wali key se match honi chahiye.
 * (Bade project me Spring Security + JWT use karte hain; abhi ke liye ye simple aur kaam karne wala hai.)
 */
@Component
public class AdminGuard {

    private final String adminKey;

    public AdminGuard(@Value("${app.admin-key}") String adminKey) {
        this.adminKey = adminKey;
    }

    public void require(String providedKey) {
        boolean ok = providedKey != null && MessageDigest.isEqual(
                adminKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8)); // constant-time compare
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect admin key");
        }
    }
}
