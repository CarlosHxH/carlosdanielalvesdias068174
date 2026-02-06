package com.album.seplag.service;

import com.album.seplag.dto.AlbumCreateDTO;
import com.album.seplag.dto.AlbumDTO;
import com.album.seplag.dto.AlbumUpdateDTO;
import com.album.seplag.dto.CapaAlbumDTO;
import com.album.seplag.dto.NotificationDTO;
import com.album.seplag.exception.ResourceNotFoundException;
import com.album.seplag.model.Album;
import com.album.seplag.model.Artista;
import com.album.seplag.model.CapaAlbum;
import com.album.seplag.model.Usuario;
import com.album.seplag.repository.AlbumRepository;
import com.album.seplag.repository.ArtistaRepository;
import com.album.seplag.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
    public AlbumDTO create(AlbumCreateDTO dto) {
        log.info("Criando novo álbum: {}", dto.titulo());
        try {
            Artista artista = artistaRepository.findById(dto.artistaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + dto.artistaId()));
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
            
            Album album = new Album();
            album.setTitulo(dto.titulo());
            album.setDataLancamento(dto.dataLancamento());
            album.setArtista(artista);
            album.setUsuario(usuario);
            Album saved = albumRepository.save(album);
            
            log.info("Álbum criado com sucesso - ID: {}, Título: {}", saved.getId(), saved.getTitulo());

            AlbumDTO savedDTO = toDTO(saved);
            NotificationDTO notification = new NotificationDTO(
                    "ALBUM_CREATED",
                    "Álbum \"" + saved.getTitulo() + "\" criado",
                    Instant.now(),
                    savedDTO
            );
            messagingTemplate.convertAndSend("/topic/albuns", notification);

            return savedDTO;
        } catch (Exception e) {
            log.error("Erro ao criar álbum: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public AlbumDTO update(Long id, AlbumUpdateDTO dto) {
        Album album = albumRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com id: " + id));
        
        album.setTitulo(dto.titulo());
        album.setDataLancamento(dto.dataLancamento());
        
        Artista artista = artistaRepository.findById(dto.artistaId()).orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + dto.artistaId()));
        album.setArtista(artista);

        Album saved = albumRepository.save(album);
        AlbumDTO savedDTO = toDTO(saved);
        NotificationDTO notification = new NotificationDTO(
                "ALBUM_UPDATED",
                "Álbum \"" + saved.getTitulo() + "\" atualizado",
                Instant.now(),
                savedDTO
        );
        messagingTemplate.convertAndSend("/topic/albuns", notification);
        return savedDTO;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando álbum com ID: {}", id);
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com id: " + id));
        String titulo = album.getTitulo();
        albumRepository.delete(album);
        log.info("Álbum deletado com sucesso - ID: {}", id);

        NotificationDTO notification = new NotificationDTO(
                "ALBUM_DELETED",
                "Álbum \"" + titulo + "\" removido",
                Instant.now(),
                Map.<String, Object>of("id", id)
        );
        messagingTemplate.convertAndSend("/topic/albuns", notification);
    }

    @Transactional
    public List<CapaAlbumDTO> uploadCapas(Long albumId, MultipartFile[] files) {
        List<CapaAlbumDTO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            CapaAlbum capa = minIOService.uploadCapa(albumId, file);
            result.add(toCapaDTO(capa));
        }
        return result;
    }

    private CapaAlbumDTO toCapaDTO(CapaAlbum capa) {

        String url = minIOService.getPresignedUrl(capa.getAlbum().getId(), capa.getId()).url();
    
        var dataUpload = capa.getDataUpload() != null
                ? capa.getDataUpload().atZone(ZoneId.systemDefault()).toInstant()
                : null;
    
        return new CapaAlbumDTO(
                capa.getId(),
                capa.getNomeArquivo(),
                capa.getContentType(),
                capa.getTamanho(),
                dataUpload,
                url
        );
    }
    

    private AlbumDTO toDTO(Album album) {

        List<CapaAlbumDTO> capas = album.getCapas().stream().map(this::toCapaDTO).collect(Collectors.toList());
    
        // String artistaFoto = null;
    
        // if (album.getArtista().getFotoNomeArquivo() != null) {
        //     artistaFoto = minIOService.getPresignedUrlFotoArtista(album.getArtista().getId()).url();
        // }
    
        return new AlbumDTO(
                album.getId(),
                album.getTitulo(),
                album.getArtista().getId(),
                album.getArtista().getNome(),
                album.getDataLancamento(),
                album.getCreatedAt(),
                capas
                // artistaFoto
        );
    }
}

