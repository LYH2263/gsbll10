package com.example.picture.entity;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * 相册成员关系：图片与相册之间的多对多从属关系（关联实体，CL-4）。
 * 绝不在 Picture 上加 albumId，也绝不复制图片记录（HD-2）。
 */
@Data
@Entity
@Table(
    name = "album_membership",
    uniqueConstraints = @UniqueConstraint(columnNames = {"albumId", "pictureId"}),
    indexes = {
        @Index(name = "idx_membership_album", columnList = "albumId"),
        @Index(name = "idx_membership_picture", columnList = "pictureId")
    }
)
public class AlbumMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long albumId;

    @Column(nullable = false)
    private Long pictureId;

    /** 相册内排序位，越小越靠前；新加入者获得最靠前位置（CL-5）。 */
    @Column(nullable = false)
    private Integer sortOrder;

    /** 加入该相册的时间，用于"最近加入"口径。 */
    private Date joinTime;
}
