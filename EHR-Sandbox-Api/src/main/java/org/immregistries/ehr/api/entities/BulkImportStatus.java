package org.immregistries.ehr.api.entities;

public class BulkImportStatus {

//    private String groupId;
//    private String registryId;
    private String status;
    private int lastAttemptCount;
    private long lastAttemptTime;
    private String result;
    private String checkUrl;

    public BulkImportStatus(String status, int lastAttemptCount, long lastAttemptTime) {
        this.status = status;
        this.lastAttemptCount = lastAttemptCount;
        this.lastAttemptTime = lastAttemptTime;
    }

    public BulkImportStatus(String result) {
        this.status = "done";
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getLastAttemptCount() {
        return lastAttemptCount;
    }

    public void setLastAttemptCount(int lastAttemptCount) {
        this.lastAttemptCount = lastAttemptCount;
    }

    public long getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(long lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }
}
