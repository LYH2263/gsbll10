package com.example.picture.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchRequest {
    private List<Long> pictureIds;
}
