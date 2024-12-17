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
    @Column(name = "raw", columnDefinition = "TEXT")
    private String rawAck = "";
    @Column(name = "rawVxu", columnDefinition = "TEXT")
    private String vxu = "";

    @Column(name = "message_id")
    private String messageId = "";
    @Column(name = "status")
    private String status = "";
    @Column(name = "sender")
    private String sender = "";
    @Column(name = "destination")
    private String destination = "";
    @Column(name = "sender_software")
    private String senderSoftware = "";
    @Column(name = "destination_software")
    private String destinationSoftware = "";


    @ManyToOne(fetch = FetchType.LAZY)
    private Facility facility;
    @ManyToOne(fetch = FetchType.LAZY)
    private EhrPatient patient;
    @ManyToOne(fetch = FetchType.LAZY)
    private VaccinationEvent vaccination;

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
        return patient;
    }

    public void setEhrPatient(EhrPatient ehrPatient) {
        this.patient = ehrPatient;
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

    public VaccinationEvent getVaccination() {
        return vaccination;
    }

    public void setVaccination(VaccinationEvent vaccinationEvent) {
        this.vaccination = vaccinationEvent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSenderSoftware() {
        return senderSoftware;
    }

    public void setSenderSoftware(String senderSoftware) {
        this.senderSoftware = senderSoftware;
    }

    public String getDestinationSoftware() {
        return destinationSoftware;
    }

    public void setDestinationSoftware(String destinationSoftware) {
        this.destinationSoftware = destinationSoftware;
    }

    public String getVxu() {
        return vxu;
    }

    public void setVxu(String vxu) {
        this.vxu = vxu;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public EhrPatient getPatient() {
        return patient;
    }

    public void setPatient(EhrPatient patient) {
        this.patient = patient;
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

