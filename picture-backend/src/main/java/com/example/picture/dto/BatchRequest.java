package com.example.picture.dto;

import java.util.List;

/** 批量加入 / 批量移出请求体。 */
public class BatchRequest {
    private List<Long> pictureIds;

    public List<Long> getPictureIds() {
        return pictureIds;
    }

    public void setPictureIds(List<Long> pictureIds) {
        this.pictureIds = pictureIds;
    }
}
