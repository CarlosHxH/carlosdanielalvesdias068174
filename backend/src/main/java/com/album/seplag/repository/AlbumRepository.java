package com.album.seplag.repository;

import com.album.seplag.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByArtistaId(Long artistaId, Pageable pageable);

    @Query("SELECT a FROM Album a WHERE a.artista.id = :artistaId")
    Page<Album> findAlbunsByArtistaId(@Param("artistaId") Long artistaId, Pageable pageable);
}

