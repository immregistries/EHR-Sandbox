package org.immregistries.ehr.api.entities;


import net.minidev.json.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class AcknowledgmentObject extends EhrEntity {
    public class SortedResult<T> {
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


    String msa_2 = "";
    SortedResult<Feedback> sortedResult = new SortedResult<>();


    public SortedResult<Feedback> getSortedResult() {
        return sortedResult;
    }

    public void setSortedResult(SortedResult<Feedback> sortedResult) {
        this.sortedResult = sortedResult;
    }

    public String getMsa_2() {
        return msa_2;
    }

    public void setMsa_2(String msa_2) {
        this.msa_2 = msa_2;
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
}

