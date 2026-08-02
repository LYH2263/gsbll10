package com.example.picture.dto;

import lombok.Data;

import java.util.List;

@Data
public class PictureIdsRequest {
    private List<Long> pictureIds;
}
