package com.album.seplag.service;

import com.album.seplag.dto.ArtistaDTO;
import com.album.seplag.exception.ResourceNotFoundException;
import com.album.seplag.model.Artista;
import com.album.seplag.repository.ArtistaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtistaService {

    private final ArtistaRepository artistaRepository;

    public ArtistaService(ArtistaRepository artistaRepository) {
        this.artistaRepository = artistaRepository;
    }

    @Transactional(readOnly = true)
    public Page<ArtistaDTO> findAll(String nome, Pageable pageable) {
        Page<Artista> artistas;
        
        if (nome != null && !nome.trim().isEmpty()) {
            artistas = artistaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            artistas = artistaRepository.findAll(pageable);
        }

        return artistas.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ArtistaDTO findById(Long id) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + id));
        return toDTO(artista);
    }

    @Transactional
    public ArtistaDTO create(Artista artista) {
        Artista saved = artistaRepository.save(artista);
        return toDTO(saved);
    }

    @Transactional
    public ArtistaDTO update(Long id, Artista artistaAtualizado) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + id));
        
        artista.setNome(artistaAtualizado.getNome());
        artista.setGenero(artistaAtualizado.getGenero());
        artista.setBiografia(artistaAtualizado.getBiografia());
        Artista saved = artistaRepository.save(artista);
        return toDTO(saved);
    }

    private ArtistaDTO toDTO(Artista artista) {
        ArtistaDTO dto = new ArtistaDTO();
        dto.setId(artista.getId());
        dto.setNome(artista.getNome());
        dto.setGenero(artista.getGenero());
        dto.setBiografia(artista.getBiografia());
        dto.setCreatedAt(artista.getCreatedAt());
        dto.setQuantidadeAlbuns((long) artista.getAlbuns().size());
        return dto;
    }
}

