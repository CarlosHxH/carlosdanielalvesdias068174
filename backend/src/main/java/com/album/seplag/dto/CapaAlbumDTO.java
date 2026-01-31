package com.album.seplag.dto;

import java.time.LocalDateTime;

public record CapaAlbumDTO(
    Long id,
    String nomeArquivo,
    String contentType,
    Long tamanho,
    LocalDateTime dataUpload,
    String presignedUrl
) {}
