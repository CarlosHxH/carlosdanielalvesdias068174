package com.album.seplag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para atualização de álbum.
 */
public record AlbumUpdateDTO(
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
    String titulo,

    @NotNull(message = "ID do artista é obrigatório")
    Long artistaId,

    LocalDate dataLancamento
) {}
