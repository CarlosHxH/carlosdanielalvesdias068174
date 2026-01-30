package com.album.seplag.controller;

import com.album.seplag.dto.ArtistaDTO;
import com.album.seplag.model.Artista;
import com.album.seplag.service.ArtistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artistas")
@Tag(name = "Artistas", description = "Endpoints para gerenciamento de artistas")
public class ArtistaController {

    private final ArtistaService artistaService;

    public ArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    @GetMapping
    @Operation(summary = "Listar artistas", description = "Lista artistas com paginação e filtro por nome")
    public ResponseEntity<Page<ArtistaDTO>> findAll(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<ArtistaDTO> artistas = artistaService.findAll(nome, pageable);
        return ResponseEntity.ok(artistas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar artista por ID", description = "Retorna detalhes de um artista")
    public ResponseEntity<ArtistaDTO> findById(@PathVariable Long id) {
        ArtistaDTO artista = artistaService.findById(id);
        return ResponseEntity.ok(artista);
    }

    @PostMapping
    @Operation(summary = "Criar artista", description = "Cria um novo artista")
    public ResponseEntity<ArtistaDTO> create(@Valid @RequestBody Artista artista) {
        ArtistaDTO created = artistaService.create(artista);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar artista", description = "Atualiza um artista existente")
    public ResponseEntity<ArtistaDTO> update(@PathVariable Long id, @Valid @RequestBody Artista artista) {
        ArtistaDTO updated = artistaService.update(id, artista);
        return ResponseEntity.ok(updated);
    }
}

