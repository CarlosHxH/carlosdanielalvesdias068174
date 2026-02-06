package com.album.seplag.controller;

import com.album.seplag.dto.NotificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Controller para testes de WebSocket.
 * Requer role ADMIN para uso.
 */
@RestController
@RequestMapping(value = "${app.api.base}/test", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("isAuthenticated()")
@Tag(name = "Teste", description = "Endpoints de teste (apenas perfil dev)")
public class WebSocketTestController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketTestController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/websocket")
    @Operation(summary = "Disparar notificação de teste", description = "Envia uma notificação de teste no tópico /topic/albuns para validar o WebSocket")
    public ResponseEntity<Map<String, String>> triggerWebSocketNotification() {
        NotificationDTO notification = new NotificationDTO(
                "TEST",
                "Notificação de teste disparada com sucesso",
                Instant.now(),
                Map.of("source", "WebSocketTestController")
        );
        messagingTemplate.convertAndSend("/topic/albuns", notification);
        return ResponseEntity.ok(Map.of(
                "message", "Notificação de teste enviada para /topic/albuns",
                "timestamp", Instant.now().toString()
        ));
    }
}
