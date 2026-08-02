package com.example.picture.repository;

import com.example.picture.entity.AlbumPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumPictureRepository extends JpaRepository<AlbumPicture, Long> {

    List<AlbumPicture> findByAlbumIdOrderBySortOrderAsc(Long albumId);

    List<AlbumPicture> findByAlbumIdIn(List<Long> albumIds);

    Optional<AlbumPicture> findByAlbumIdAndPictureId(Long albumId, Long pictureId);

    long countByAlbumId(Long albumId);

    @Modifying
    @Query("delete from AlbumPicture ap where ap.albumId = :albumId")
    void deleteByAlbumId(@Param("albumId") Long albumId);

    @Modifying
    @Query("delete from AlbumPicture ap where ap.pictureId = :pictureId")
    void deleteByPictureId(@Param("pictureId") Long pictureId);

    @Query("select coalesce(min(ap.sortOrder), 0) from AlbumPicture ap where ap.albumId = :albumId")
    Integer findMinSortOrder(@Param("albumId") Long albumId);

    @Query("select distinct ap.albumId from AlbumPicture ap where ap.pictureId = :pictureId")
    List<Long> findAlbumIdsByPictureId(@Param("pictureId") Long pictureId);
}
