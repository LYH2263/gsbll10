package com.example.picture.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * 相册-图片成员关系（多对多）。
 * 一张图片可属于多个相册；同一图片在同一相册中至多出现一次。
 * 仅承载关系、排序与加入时间，绝不复制图片记录。
 */
@Data
@Entity
@Table(name = "album_picture", uniqueConstraints = @UniqueConstraint(columnNames = {"album_id", "picture_id"}))
public class AlbumPicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "album_id", nullable = false)
    private Long albumId;
    @Column(name = "picture_id", nullable = false)
    private Long pictureId;
    /**
     * 相册内排序位，越小越靠前；新加入者取最靠前位置
     */
    private Integer sortOrder;
    /**
     * 加入该相册的时间，用于"最近加入"口径
     */
    private Date joinTime;
}
