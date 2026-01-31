package com.album.seplag.dto;

public record PresignedUrlResponse(
    String url,
    Long expiresIn
) {}
