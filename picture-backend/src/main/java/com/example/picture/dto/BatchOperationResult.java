package com.example.picture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResult {
    private BatchResultDTO result;
    private AlbumDTO album;
    private AlbumDetailDTO detail;
}
