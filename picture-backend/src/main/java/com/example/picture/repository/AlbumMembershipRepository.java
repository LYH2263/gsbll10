package com.example.picture.repository;

import com.example.picture.entity.AlbumMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AlbumMembershipRepository extends JpaRepository<AlbumMembership, Long> {

    /** 相册内成员，按 sortOrder 升序（相册内展示顺序）。 */
    List<AlbumMembership> findByAlbumIdOrderBySortOrderAsc(Long albumId);

    List<AlbumMembership> findByAlbumId(Long albumId);

    AlbumMembership findByAlbumIdAndPictureId(Long albumId, Long pictureId);

    long countByAlbumId(Long albumId);

    /** 某张图片涉及的所有成员关系（图库删除图片时用于连带清理）。 */
    List<AlbumMembership> findByPictureId(Long pictureId);

    @Transactional
    void deleteByAlbumId(Long albumId);

    @Transactional
    void deleteByPictureId(Long pictureId);
}
