package com.example.picture.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class BatchResult {
    private int requested;
    private List<Long> succeeded = new ArrayList<Long>();
    private List<Long> ignored = new ArrayList<Long>();
    private List<FailedItem> failed = new ArrayList<FailedItem>();

    @Data
    public static class FailedItem {
        private Long pictureId;
        private String reason;

        public FailedItem(Long pictureId, String reason) {
            this.pictureId = pictureId;
            this.reason = reason;
        }
    }
}
