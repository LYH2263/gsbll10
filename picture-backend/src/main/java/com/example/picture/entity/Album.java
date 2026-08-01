package com.example.picture.entity;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * 相册实体。相册是图片之上的逻辑分组（视图），不物理持有图片。
 * 成员关系由独立的 {@link AlbumMembership} 表达（多对多，HD-2）。
 */
@Data
@Entity
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 相册名，1~50 字符，去空白后非空；允许重名（CL-4）。 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 可选简介，≤200 字符。 */
    @Column(length = 200)
    private String description;

    /** 手动指定的封面图片 id，可空；展示封面按回退规则计算（CL-5）。 */
    private Long coverPictureId;

    private Date createTime;

    private Date updateTime;
}
