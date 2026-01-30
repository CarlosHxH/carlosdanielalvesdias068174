package com.album.seplag.service;

import com.album.seplag.dto.AlbumDTO;
import com.album.seplag.dto.CapaAlbumDTO;
import com.album.seplag.exception.ResourceNotFoundException;
import com.album.seplag.model.Album;
import com.album.seplag.model.Artista;
import com.album.seplag.model.Usuario;
import com.album.seplag.repository.AlbumRepository;
import com.album.seplag.repository.ArtistaRepository;
import com.album.seplag.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MinIOService minIOService;
    private final SimpMessagingTemplate messagingTemplate;

    public AlbumService(AlbumRepository albumRepository, ArtistaRepository artistaRepository,
                       UsuarioRepository usuarioRepository, MinIOService minIOService, 
                       SimpMessagingTemplate messagingTemplate) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.minIOService = minIOService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findAll(Pageable pageable) {
        return albumRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByArtistaId(Long artistaId, Pageable pageable) {
        return albumRepository.findByArtistaId(artistaId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public AlbumDTO findById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com id: " + id));
        return toDTO(album);
    }

    @Transactional
    public AlbumDTO create(Album album) {
        Artista artista = artistaRepository.findById(album.getArtista().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + album.getArtista().getId()));
        
        // Obter usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
        
        album.setArtista(artista);
        album.setUsuario(usuario);
        Album saved = albumRepository.save(album);
        
        // Notificar via WebSocket
        messagingTemplate.convertAndSend("/topic/albuns", toDTO(saved));
        
        return toDTO(saved);
    }

    @Transactional
    public AlbumDTO update(Long id, Album albumAtualizado) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com id: " + id));
        
        album.setTitulo(albumAtualizado.getTitulo());
        album.setDataLancamento(albumAtualizado.getDataLancamento());
        
        if (albumAtualizado.getArtista() != null && albumAtualizado.getArtista().getId() != null) {
            Artista artista = artistaRepository.findById(albumAtualizado.getArtista().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + albumAtualizado.getArtista().getId()));
            album.setArtista(artista);
        }
        
        Album saved = albumRepository.save(album);
        return toDTO(saved);
    }

    private AlbumDTO toDTO(Album album) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(album.getId());
        dto.setTitulo(album.getTitulo());
        dto.setArtistaId(album.getArtista().getId());
        dto.setArtistaNome(album.getArtista().getNome());
        dto.setDataLancamento(album.getDataLancamento());
        dto.setCreatedAt(album.getCreatedAt());
        
        dto.setCapas(album.getCapas().stream()
                .map(capa -> {
                    CapaAlbumDTO capaDTO = new CapaAlbumDTO();
                    capaDTO.setId(capa.getId());
                    capaDTO.setNomeArquivo(capa.getNomeArquivo());
                    capaDTO.setContentType(capa.getContentType());
                    capaDTO.setTamanho(capa.getTamanho());
                    capaDTO.setDataUpload(capa.getDataUpload());
                    return capaDTO;
                })
                .collect(Collectors.toList()));
        
        return dto;
    }
}

