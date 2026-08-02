package com.example.picture.dto;

import com.example.picture.entity.Picture;

import java.util.List;

/**
 * 相册详情视图：相册概要 + 该相册内图片（按 sortOrder 排列）。
 * 图片为图库中的同一批实体（同 id、同文件），非副本。
 */
public class AlbumDetailView {
    private AlbumView album;
    private List<Picture> pictures;

    public AlbumDetailView(AlbumView album, List<Picture> pictures) {
        this.album = album;
        this.pictures = pictures;
    }

    public AlbumView getAlbum() {
        return album;
    }

    public void setAlbum(AlbumView album) {
        this.album = album;
    }

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }
}
