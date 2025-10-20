package com.s406.livon.global.web.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 페이징 처리된 API 응답 처리 Dto
 *
 * @param page       현재 페이지 번호 (0부터 시작)
 * @param pageSize   페이지 당 아이템 수
 * @param totalItems 모든 페이지에 걸친 총 아이템 수
 * @param totalPages 총 페이지 수
 * @param items      현재 페이지의 아이템 목록
 *
 */
@Builder
public record PaginatedResponse<T>(
        int page,
        int pageSize,
        long totalItems,
        int totalPages,
        List<T> items
) {
    // Page<E> -> PaginatedResponse<T> 생성
    public static <T> PaginatedResponse<T> of(Page<T> page) {
        return PaginatedResponse.<T>builder()
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(page.getContent())
                .build();
    }

    // Page<E> -> PaginatedResponse<T> 매핑
    public static <E, T> PaginatedResponse<T> of(Page<E> page, Function<? super E, ? extends T> mapper) {
        List<T> mapped = page.getContent()
                .stream()
                .map(mapper)
                .collect(Collectors.toList());

        return PaginatedResponse.<T>builder()
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(mapped)
                .build();
    }
}

