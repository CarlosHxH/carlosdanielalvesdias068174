package com.album.seplag.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String type,
    Long expiresIn
) {
    public LoginResponse(String accessToken, String refreshToken, Long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
