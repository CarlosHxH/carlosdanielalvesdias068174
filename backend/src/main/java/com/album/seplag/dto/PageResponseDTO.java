package com.album.seplag.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO de resposta paginada com estrutura estável para serialização JSON.
 * Evita dependência da serialização interna do Spring Data Page/PageImpl.
 */
public record PageResponseDTO<T>(
    List<T> content,
    int number,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PageResponseDTO<T> of(Page<T> page) {
        return new PageResponseDTO<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
