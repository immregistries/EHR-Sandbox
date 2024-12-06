package org.immregistries.ehr.api.entities;


import java.util.ArrayList;
import java.util.List;

public class AcknowledgmentObject extends EhrEntity {
    String msa_2 = "";

    List<Feedback> errors = new ArrayList<>(4);
    List<Feedback> warnings = new ArrayList<>(4);
    List<Feedback> notices = new ArrayList<>(4);
    List<Feedback> infos = new ArrayList<>(4);

    public AcknowledgmentObject() {
    }

    public List<Feedback> getErrors() {
        return errors;
    }

    public void setErrors(List<Feedback> errors) {
        this.errors = errors;
    }

    public List<Feedback> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Feedback> warnings) {
        this.warnings = warnings;
    }

    public List<Feedback> getNotices() {
        return notices;
    }

    public void setNotices(List<Feedback> notices) {
        this.notices = notices;
    }

    public List<Feedback> getInfos() {
        return infos;
    }

    public void setInfos(List<Feedback> infos) {
        this.infos = infos;
    }
}
