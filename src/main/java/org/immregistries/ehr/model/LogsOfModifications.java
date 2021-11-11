package org.immregistries.ehr.model;
import java.io.Serializable;
import java.util.Date;

public class LogsOfModifications implements Serializable {

    private static final long serialVersionUID = 1L;

    private int logId = 0;
    private String modifType = "";
    private Date modifDate = null;

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getModifType() {
        return modifType;
    }

    public void setModifType(String modifType) {
        this.modifType = modifType;
    }

    public Date getModifDate() {
        return modifDate;
    }

    public void setModifDate(Date modifDate) {
        this.modifDate = modifDate;
    }
}