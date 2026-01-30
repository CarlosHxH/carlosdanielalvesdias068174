package com.album.seplag.repository;

import com.album.seplag.model.CapaAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapaAlbumRepository extends JpaRepository<CapaAlbum, Long> {
    List<CapaAlbum> findByAlbumId(Long albumId);
}

