package com.example.picture.dto;

import com.example.picture.entity.Picture;

import java.util.Date;

/**
 * 相册列表 / 概要视图。cover 为计算后的展示封面（03 契约 §4），可为 null。
 */
public class AlbumView {
    private Long id;
    private String name;
    private String description;
    private Long coverPictureId;
    private Picture cover;
    private long pictureCount;
    private Date createTime;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCoverPictureId() {
        return coverPictureId;
    }

    public void setCoverPictureId(Long coverPictureId) {
        this.coverPictureId = coverPictureId;
    }

    public Picture getCover() {
        return cover;
    }

    public void setCover(Picture cover) {
        this.cover = cover;
    }

    public long getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(long pictureCount) {
        this.pictureCount = pictureCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
