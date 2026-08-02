package com.example.picture.dto;

import com.example.picture.entity.Picture;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class AlbumDetailView {
    private Long id;
    private String name;
    private String description;
    private Long coverPictureId;
    private AlbumView.CoverInfo cover;
    private Integer pictureCount;
    private Date createTime;
    private Date updateTime;
    private List<Picture> pictures;
}
