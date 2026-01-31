package com.album.seplag.dto;

public record LoginResponse(
    String token,
    String type,
    Long expiresIn
) {
    public LoginResponse(String token, Long expiresIn) {
        this(token, "Bearer", expiresIn);
    }
}
