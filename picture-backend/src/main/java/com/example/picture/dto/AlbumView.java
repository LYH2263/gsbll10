package com.example.picture.dto;

import lombok.Data;
import java.util.Date;

@Data
public class AlbumView {
    private Long id;
    private String name;
    private String description;
    private Long coverPictureId;
    private CoverInfo cover;
    private Integer pictureCount;
    private Date createTime;
    private Date updateTime;

    @Data
    public static class CoverInfo {
        private Long id;
        private String name;
        private String url;
    }
}
