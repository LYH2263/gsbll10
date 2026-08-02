package com.example.picture.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Data
@Entity
@Table(name = "album_membership", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"albumId", "pictureId"})
})
public class AlbumMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long albumId;
    private Long pictureId;
    private Integer sortOrder;
    private Date joinTime;
}
