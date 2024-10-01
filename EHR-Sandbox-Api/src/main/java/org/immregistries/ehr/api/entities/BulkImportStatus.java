package org.immregistries.ehr.api.entities;

import java.util.Date;

public class BulkImportStatus {

    //    private String groupId;
//    private Integer registryId;
    private String status;
    private int lastAttemptCount;
    private long lastAttemptTime;
    private long delay;
    private String result;
    private String checkUrl;

    public static BulkImportStatus success(String result) {
        BulkImportStatus bulkImportStatus = new BulkImportStatus();
        bulkImportStatus.setStatus("done");
        bulkImportStatus.setResult(result);
        return bulkImportStatus;
    }

    public static BulkImportStatus started(String checkUrl) {
        BulkImportStatus bulkImportStatus = new BulkImportStatus();
        bulkImportStatus.setStatus("Started");
        bulkImportStatus.setLastAttemptCount(0);
        bulkImportStatus.setLastAttemptTime(new Date().getTime());
        bulkImportStatus.setCheckUrl(checkUrl);
        return bulkImportStatus;
    }

    public static BulkImportStatus inProgress(int lastAttemptCount, String checkUrl) {
        BulkImportStatus bulkImportStatus = new BulkImportStatus();
        bulkImportStatus.setStatus("In Progress");
        bulkImportStatus.setLastAttemptCount(lastAttemptCount);
        bulkImportStatus.setLastAttemptTime(new Date().getTime());
        bulkImportStatus.setCheckUrl(checkUrl);
        return bulkImportStatus;
    }

    public BulkImportStatus() {

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
