package com.example.picture.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@Entity
@Table(name = "album_picture", uniqueConstraints = {
        @UniqueConstraint(name = "uk_album_picture", columnNames = {"albumId", "pictureId"})
})
public class AlbumPicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long albumId;

    private Long pictureId;

    private Integer sortOrder;

    @Temporal(TemporalType.TIMESTAMP)
    private Date joinTime;
}
