package com.example.picture.dto;

import java.util.List;

/** 更新相册内顺序请求体：pictureIds 给出期望的展示顺序（越靠前越靠上）。 */
public class OrderRequest {
    private List<Long> pictureIds;

    public List<Long> getPictureIds() {
        return pictureIds;
    }

    public void setPictureIds(List<Long> pictureIds) {
        this.pictureIds = pictureIds;
    }
}
