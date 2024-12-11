package org.immregistries.ehr.api.entities;


import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.minidev.json.annotate.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity()
@Table(name = "ehr_group")
public class AcknowledgmentObject extends EhrEntity {

    private String messageId = "";
    private String status = "";
    private EhrPatient ehrPatient;
    private SortedResult<Feedback> sortedResult = new SortedResult<>();
    private String iis = "";
    private Timestamp timestamp;


    public SortedResult<Feedback> getSortedResult() {
        return sortedResult;
    }

    @JsonIgnore
    public void setSortedResult(SortedResult<Feedback> sortedResult) {
        this.sortedResult = sortedResult;
    }

    public String getMsa_2() {
        return status;
    }

    public void setMsa_2(String msa_2) {
        this.status = msa_2;
    }


    public AcknowledgmentObject() {
    }

    @JsonIgnore
    public List<Feedback> getErrors() {
        return getSortedResult().getErrors();
    }

    public void setErrors(List<Feedback> errors) {
        this.getSortedResult().setErrors(errors);
    }

    @JsonIgnore
    public List<Feedback> getWarnings() {
        return getSortedResult().getWarnings();
    }

    public void setWarnings(List<Feedback> warnings) {
        this.getSortedResult().setWarnings(warnings);
    }

    @JsonIgnore
    public List<Feedback> getNotices() {
        return getSortedResult().getNotices();
    }

    public void setNotices(List<Feedback> notices) {
        this.getSortedResult().setNotices(notices);
    }

    @JsonIgnore
    public List<Feedback> getInfos() {
        return getSortedResult().getInfos();
    }

    public void setInfos(List<Feedback> infos) {
        this.getSortedResult().setInfos(infos);
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public EhrPatient getEhrPatient() {
        return ehrPatient;
    }

    public void setEhrPatient(EhrPatient ehrPatient) {
        this.ehrPatient = ehrPatient;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIis() {
        return iis;
    }

    public void setIis(String iis) {
        this.iis = iis;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Embeddable
    public static class SortedResult<T> implements Serializable {

        List<T> errors = new ArrayList<>(4);
        List<T> warnings = new ArrayList<>(4);
        List<T> notices = new ArrayList<>(4);
        List<T> infos = new ArrayList<>(4);

        public List<T> getErrors() {
            return errors;
        }

        public void setErrors(List<T> errors) {
            this.errors = errors;
        }

        public List<T> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<T> warnings) {
            this.warnings = warnings;
        }

        public List<T> getNotices() {
            return notices;
        }

        public void setNotices(List<T> notices) {
            this.notices = notices;
        }

        public List<T> getInfos() {
            return infos;
        }

        public void setInfos(List<T> infos) {
            this.infos = infos;
        }
    }
}

