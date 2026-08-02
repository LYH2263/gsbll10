package com.example.picture.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchResultDTO {
    private int requested;
    private List<Long> succeeded = new ArrayList<Long>();
    private List<Long> ignored = new ArrayList<Long>();
    private List<BatchFailedItem> failed = new ArrayList<BatchFailedItem>();
}
