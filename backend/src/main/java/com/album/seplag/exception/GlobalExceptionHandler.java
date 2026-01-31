package com.album.seplag.exception;

import com.album.seplag.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String message = errors.entrySet().stream()
            .map(entry -> "Campo '" + entry.getKey() + "': " + entry.getValue())
            .findFirst()
            .orElse("Erro de validação");
        
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Erro de Validação",
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(
            PropertyReferenceException ex,
            HttpServletRequest request) {
        String path = request.getRequestURI();
        String entityType = detectEntityType(path);
        String validProperties = getValidPropertiesForEntity(entityType);
        
        String message = String.format(
            "Propriedade de ordenação inválida: '%s'. Propriedades válidas para %s: %s",
            ex.getPropertyName(),
            entityType,
            validProperties
        );
        
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Parâmetro de Ordenação Inválido",
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsageException(
            InvalidDataAccessApiUsageException ex,
            HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && message.contains("sort")) {
            message = "Formato de ordenação inválido. Use: sort=propriedade,direção (ex: sort=nome,asc ou sort=nome,desc)";
        }
        
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Parâmetro de Paginação Inválido",
            message != null ? message : "Parâmetros de paginação inválidos",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String error = "Bad Request";
        
        if (message != null && (message.contains("Credenciais inválidas") || message.contains("não encontrado"))) {
            status = HttpStatus.UNAUTHORIZED;
            error = "Unauthorized";
            message = "Credenciais inválidas";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            status.value(),
            error,
            message != null ? message : "Erro ao processar requisição",
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Erro interno do servidor: " + ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String detectEntityType(String path) {
        if (path.contains("/artistas")) {
            return "Artista";
        } else if (path.contains("/albuns")) {
            return "Album";
        }
        return "a entidade";
    }

    private String getValidPropertiesForEntity(String entityType) {
        switch (entityType) {
            case "Artista":
                return "id, nome, genero, createdAt, updatedAt";
            case "Album":
                return "id, titulo, dataLancamento, createdAt, updatedAt";
            default:
                return "consulte a documentação da API";
        }
    }
}
