package com.example.picture.dto;

import com.example.picture.entity.Picture;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AlbumDetailDTO {
    private Long id;
    private String name;
    private String description;
    private Long coverPictureId;
    private CoverDTO cover;
    private long pictureCount;
    private Date createTime;
    private Date updateTime;
    private List<Picture> pictures;

    public static AlbumDetailDTO from(AlbumDTO album, List<Picture> pictures) {
        AlbumDetailDTO dto = new AlbumDetailDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setDescription(album.getDescription());
        dto.setCoverPictureId(album.getCoverPictureId());
        dto.setCover(album.getCover());
        dto.setPictureCount(album.getPictureCount());
        dto.setCreateTime(album.getCreateTime());
        dto.setUpdateTime(album.getUpdateTime());
        dto.setPictures(pictures);
        return dto;
    }
}
