package com.example.picture.repository;

import com.example.picture.entity.AlbumMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AlbumMembershipRepository extends JpaRepository<AlbumMembership, Long> {

    List<AlbumMembership> findByAlbumIdOrderBySortOrderAsc(Long albumId);

    Optional<AlbumMembership> findByAlbumIdAndPictureId(Long albumId, Long pictureId);

    long countByAlbumId(Long albumId);

    @Modifying
    @Query("DELETE FROM AlbumMembership m WHERE m.albumId = :albumId")
    void deleteAllByAlbumId(@Param("albumId") Long albumId);

    @Modifying
    @Query("DELETE FROM AlbumMembership m WHERE m.albumId = :albumId AND m.pictureId = :pictureId")
    void deleteByAlbumIdAndPictureId(@Param("albumId") Long albumId, @Param("pictureId") Long pictureId);

    @Modifying
    @Query("DELETE FROM AlbumMembership m WHERE m.pictureId = :pictureId")
    void deleteAllByPictureId(@Param("pictureId") Long pictureId);

    @Query("SELECT COALESCE(MAX(m.sortOrder), 0) FROM AlbumMembership m WHERE m.albumId = :albumId")
    Integer findMaxSortOrderByAlbumId(@Param("albumId") Long albumId);

    @Query("SELECT COALESCE(MIN(m.sortOrder), 0) FROM AlbumMembership m WHERE m.albumId = :albumId")
    Integer findMinSortOrderByAlbumId(@Param("albumId") Long albumId);

    List<AlbumMembership> findByAlbumIdOrderByJoinTimeDesc(Long albumId);

    @Query("SELECT m.pictureId FROM AlbumMembership m WHERE m.albumId = :albumId")
    List<Long> findPictureIdsByAlbumId(@Param("albumId") Long albumId);
}
