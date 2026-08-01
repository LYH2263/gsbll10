package com.example.picture.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 批处理返回结构（03 契约 §5 / CL-6）：部分成功语义。
 * requested / succeeded / ignored / failed。
 */
public class BatchResult {
    private int requested;
    private List<Long> succeeded = new ArrayList<Long>();
    private List<Long> ignored = new ArrayList<Long>();
    private List<FailedItem> failed = new ArrayList<FailedItem>();

    /** 无法处理的项：pictureId + 可读中文原因。 */
    public static class FailedItem {
        private Long pictureId;
        private String reason;

        public FailedItem() {
        }

        public FailedItem(Long pictureId, String reason) {
            this.pictureId = pictureId;
            this.reason = reason;
        }

        public Long getPictureId() {
            return pictureId;
        }

        public void setPictureId(Long pictureId) {
            this.pictureId = pictureId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public int getRequested() {
        return requested;
    }

    public void setRequested(int requested) {
        this.requested = requested;
    }

    public List<Long> getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(List<Long> succeeded) {
        this.succeeded = succeeded;
    }

    public List<Long> getIgnored() {
        return ignored;
    }

    public void setIgnored(List<Long> ignored) {
        this.ignored = ignored;
    }

    public List<FailedItem> getFailed() {
        return failed;
    }

    public void setFailed(List<FailedItem> failed) {
        this.failed = failed;
    }
}
