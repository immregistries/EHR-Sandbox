package org.immregistries.ehr.api.entities;


import jakarta.persistence.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.Filter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

//@Entity()
//@Table(name = "acknowledgment_object")
public class AcknowledgmentObject extends EhrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ack_id", nullable = false)
    private Integer id;

    @Column(name = "message_id")
    private String messageId = "";
    @Column(name = "status")
    private String status = "";
    @Column(name = "status", columnDefinition = "TEXT")
    private String rawAck = "";
    @ManyToOne(fetch = FetchType.LAZY)
    private EhrPatient ehrPatient;
    @ManyToOne(fetch = FetchType.LAZY)
    private VaccinationEvent vaccinationEvent;

    @Embedded
    private SortedResult sortedResult = new SortedResult();

    @Column(name = "iis")
    private String iis = "";
    @JoinColumn(name = "timestamp", nullable = false)
    private Timestamp timestamp;


    public SortedResult getSortedResult() {
        return sortedResult;
    }

    @JsonIgnore
    public void setSortedResult(SortedResult sortedResult) {
        this.sortedResult = sortedResult;
    }

    public String getMsa_2() {
        return status;
    }

    public void setMsa_2(String msa_2) {
        this.status = msa_2;
    }


    public AcknowledgmentObject() {
//        this.timestamp = new Timestamp(new Date().getTime());
    }

    public AcknowledgmentObject(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public Set<Feedback> getErrors() {
        return getSortedResult().getErrors();
    }

    public void setErrors(Set<Feedback> errors) {
        this.getSortedResult().setErrors(errors);
    }

    @JsonIgnore
    public Set<Feedback> getWarnings() {
        return getSortedResult().getWarnings();
    }

    public void setWarnings(Set<Feedback> warnings) {
        this.getSortedResult().setWarnings(warnings);
    }

    @JsonIgnore
    public Set<Feedback> getNotices() {
        return getSortedResult().getNotices();
    }

    public void setNotices(Set<Feedback> notices) {
        this.getSortedResult().setNotices(notices);
    }

    @JsonIgnore
    public Set<Feedback> getInfos() {
        return getSortedResult().getInfos();
    }

    public void setInfos(Set<Feedback> infos) {
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

    public String getRawAck() {
        return rawAck;
    }

    public void setRawAck(String rawAck) {
        this.rawAck = rawAck;
    }

    public VaccinationEvent getVaccinationEvent() {
        return vaccinationEvent;
    }

    public void setVaccinationEvent(VaccinationEvent vaccinationEvent) {
        this.vaccinationEvent = vaccinationEvent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Embeddable
    public static class SortedResult implements Serializable {

        @OneToMany
//        @JoinTable
        //filter on the target entity table
        @Filter(name = "severityError", condition = ":severity = 'E'")
        Set<Feedback> errors = new HashSet<>(4);
        @Filter(name = "severityW", condition = ":warning = 'W'")
        Set<Feedback> warnings = new HashSet<>(4);
        @Filter(name = "severityN", condition = ":warning = 'N'")
        Set<Feedback> notices = new HashSet<>(4);
        @Filter(name = "severityI", condition = ":warning = 'I'")
        Set<Feedback> infos = new HashSet<>(4);

        public Set<Feedback> getErrors() {
            return errors;
        }

        public void setErrors(Set<Feedback> errors) {
            this.errors = errors;
        }

        public Set<Feedback> getWarnings() {
            return warnings;
        }

        public void setWarnings(Set<Feedback> warnings) {
            this.warnings = warnings;
        }

        public Set<Feedback> getNotices() {
            return notices;
        }

        public void setNotices(Set<Feedback> notices) {
            this.notices = notices;
        }

        public Set<Feedback> getInfos() {
            return infos;
        }

        public void setInfos(Set<Feedback> infos) {
            this.infos = infos;
        }
    }
}

