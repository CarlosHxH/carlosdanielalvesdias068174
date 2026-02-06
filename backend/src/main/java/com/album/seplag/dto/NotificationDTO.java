package com.album.seplag.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO para mensagens de notificação via WebSocket.
 * Compatível com o formato esperado pelo frontend (NotificationMessage).
 */
public record NotificationDTO(
        String type,
        String message,
        String timestamp,
        Map<String, Object> data
) {
    public NotificationDTO(String type, String message, Instant timestamp, Object data) {
        this(
                type,
                message,
                timestamp != null ? timestamp.toString() : Instant.now().toString(),
                data != null ? Map.of("payload", data) : null
        );
    }

    public NotificationDTO(String type, String message, Instant timestamp) {
        this(type, message, timestamp, (Object) null);
    }

    public NotificationDTO(String type, String message, Instant timestamp, Map<String, Object> data) {
        this(
                type,
                message,
                timestamp != null ? timestamp.toString() : Instant.now().toString(),
                data
        );
    }
}
