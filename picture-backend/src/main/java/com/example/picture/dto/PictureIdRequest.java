package com.example.picture.dto;

/** 加入单张图片请求体。 */
public class PictureIdRequest {
    private Long pictureId;

    public Long getPictureId() {
        return pictureId;
    }

    public void setPictureId(Long pictureId) {
        this.pictureId = pictureId;
    }
}
