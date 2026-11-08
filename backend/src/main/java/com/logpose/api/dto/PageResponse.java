package com.logpose.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A stable JSON shape for paged results (Spring's Page serializes with internal details).
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
