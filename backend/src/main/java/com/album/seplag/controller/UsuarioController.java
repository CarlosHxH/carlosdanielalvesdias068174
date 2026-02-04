package com.album.seplag.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.album.seplag.dto.AlterarSenhaAdminDTO;
import com.album.seplag.dto.AlterarSenhaDTO;
import com.album.seplag.dto.PageResponseDTO;
import com.album.seplag.dto.UsuarioCreateDTO;
import com.album.seplag.dto.UsuarioDTO;
import com.album.seplag.dto.UsuarioUpdateDTO;
import com.album.seplag.enums.SortDirection;
import com.album.seplag.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "${app.api.base}/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil do usuário logado", description = "Retorna dados do usuário autenticado")
    public ResponseEntity<UsuarioDTO> getMe() {
        String username = getCurrentUsername();
        UsuarioDTO usuario = usuarioService.findByUsername(username);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar próprio perfil", description = "Usuário atualiza seus próprios dados")
    public ResponseEntity<UsuarioDTO> updateMe(@Valid @RequestBody UsuarioUpdateDTO dto) {
        String username = getCurrentUsername();
        UsuarioDTO usuario = usuarioService.updateMe(username, dto);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/me/senha")
    @Operation(summary = "Alterar própria senha", description = "Usuário altera sua própria senha")
    public ResponseEntity<Void> alterarMinhaSenha(@Valid @RequestBody AlterarSenhaDTO dto) {
        String username = getCurrentUsername();
        usuarioService.alterarSenha(username, dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Listar usuários", description = "Lista usuários com paginação (apenas ADMIN)")
    public ResponseEntity<PageResponseDTO<UsuarioDTO>> findAll(
            @Parameter(description = "Número da página (começa em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Direção da ordenação")
            @RequestParam(defaultValue = "ASC") SortDirection direction) {

        Sort.Direction sortDirection = direction == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(PageResponseDTO.of(usuarioService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna detalhes de um usuário (apenas ADMIN)")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        UsuarioDTO usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário (apenas ADMIN)")
    public ResponseEntity<UsuarioDTO> create(@Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioDTO created = usuarioService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Atualizar usuário", description = "Atualiza um usuário (apenas ADMIN). O ID do path tem precedência; o body não deve conter id.")
    public ResponseEntity<UsuarioDTO> update(
            @Parameter(description = "ID do usuário (path tem precedência sobre qualquer id no body)")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        UsuarioDTO updated = usuarioService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Remover usuário", description = "Remove um usuário (apenas ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Ativar/Desativar usuário", description = "Alterna status ativo do usuário (apenas ADMIN)")
    public ResponseEntity<UsuarioDTO> toggleAtivo(@PathVariable Long id) {
        UsuarioDTO usuario = usuarioService.toggleAtivo(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/senha")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Admin alterar senha", description = "Admin altera senha de qualquer usuário")
    public ResponseEntity<Void> alterarSenhaAdmin(@PathVariable Long id, @Valid @RequestBody AlterarSenhaAdminDTO dto) {
        usuarioService.alterarSenhaAdmin(id, dto);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        return authentication.getName();
    }
}
