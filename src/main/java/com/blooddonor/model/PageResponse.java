package com.blooddonor.model;

import org.springframework.data.domain.Page;

import java.util.List;

/** Frontend ko simple pagination JSON dene ke liye chhota wrapper. */
public record PageResponse<T>(List<T> content, int page, int totalPages, long totalElements) {

    public static <T> PageResponse<T> of(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getTotalPages(), p.getTotalElements());
    }
}
