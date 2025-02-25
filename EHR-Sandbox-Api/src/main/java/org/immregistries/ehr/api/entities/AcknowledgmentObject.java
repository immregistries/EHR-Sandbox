package org.immregistries.ehr.api.entities;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity()
@Table(name = "acknowledgment_object")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = AcknowledgmentObject.class)
//TODO Persist
public class AcknowledgmentObject extends EhrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Explicitly annotate the ID column
    private Integer id;

    @Column(columnDefinition = "TEXT", name = "raw")
    private String raw = "";

    @Column(columnDefinition = "TEXT", name = "vxu")
    private String vxu = "";

    @Column(name = "message_id")
    private String messageId = "";

    @Column(name = "status") // Added @Column
    private String status = "";

    @Column(name = "sender") // Added @Column
    private String sender = "";

    @Column(name = "destination") // Added @Column
    private String destination = "";

    @Column(name = "sender_software")
    private String senderSoftware = "";

    @Column(name = "destination_software")
    private String destinationSoftware = "";

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    @JsonIdentityReference(alwaysAsId = true)
    private EhrPatient patient;

    @ManyToOne
    @JoinColumn(name = "vaccination_id")
    @JsonIdentityReference(alwaysAsId = true)
    private VaccinationEvent vaccination;

    @Embedded
    private SortedResult sortedResult = new SortedResult();

    @Column(name = "iis") // Added @Column
    private String iis = "";

    @Column(name = "timestamp") // Added @Column
    private Timestamp timestamp;

    @Embedded
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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
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
        @OneToMany(mappedBy = "acknowledgmentObject", cascade = CascadeType.ALL, orphanRemoval = true)
        @SQLRestriction("severity = 'E'")
        Set<Feedback> errors = new HashSet<>(4);

        @OneToMany(mappedBy = "acknowledgmentObject", cascade = CascadeType.ALL, orphanRemoval = true)
        @SQLRestriction("severity = 'W'")
        Set<Feedback> warnings = new HashSet<>(4);

        @OneToMany(mappedBy = "acknowledgmentObject", cascade = CascadeType.ALL, orphanRemoval = true)
        @SQLRestriction("severity = 'N'")
        Set<Feedback> notices = new HashSet<>(4);

        @OneToMany(mappedBy = "acknowledgmentObject", cascade = CascadeType.ALL, orphanRemoval = true)
        @SQLRestriction("severity = 'I'")
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

