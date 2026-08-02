package com.example.picture.dto;

import com.example.picture.entity.Album;
import lombok.Data;

import java.util.Date;

@Data
public class AlbumDTO {
    private Long id;
    private String name;
    private String description;
    private Long coverPictureId;
    private CoverDTO cover;
    private long pictureCount;
    private Date createTime;
    private Date updateTime;

    public static AlbumDTO from(Album album, CoverDTO cover, long pictureCount) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setDescription(album.getDescription());
        dto.setCoverPictureId(album.getCoverPictureId());
        dto.setCover(cover);
        dto.setPictureCount(pictureCount);
        dto.setCreateTime(album.getCreateTime());
        dto.setUpdateTime(album.getUpdateTime());
        return dto;
    }
}
