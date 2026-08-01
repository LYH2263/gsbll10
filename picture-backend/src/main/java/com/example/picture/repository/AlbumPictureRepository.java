package com.example.picture.repository;

import com.example.picture.entity.AlbumPicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumPictureRepository extends JpaRepository<AlbumPicture, Long> {

    List<AlbumPicture> findByAlbumIdOrderBySortOrderAsc(Long albumId);

    List<AlbumPicture> findByAlbumId(Long albumId);

    List<AlbumPicture> findByPictureId(Long pictureId);

    Optional<AlbumPicture> findByAlbumIdAndPictureId(Long albumId, Long pictureId);

    void deleteByAlbumId(Long albumId);

    void deleteByPictureId(Long pictureId);
}
