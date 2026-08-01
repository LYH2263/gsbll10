package com.example.picture.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(length = 200)
    private String description;
    /**
     * 手动指定的封面图片 id，可为空；为空时展示封面回退为"最近加入的成员"
     */
    private Long coverPictureId;
    private Date createTime;
    private Date updateTime;
}
