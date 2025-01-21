package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.immregistries.ehr.api.entities.embedabbles.Hl7Location;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "feedback"
//        , indexes = {
//        @Index(name = "fk_vaccination_event_idx", columnList = "vaccination_event_id"),
//        @Index(name = "fk_facility_idx", columnList = "facility_id"),
//        @Index(name = "patient_id_idx", columnList = "patient_id")
//}
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Feedback.class)
public class Feedback extends EhrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "feedback_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("patient")
    private EhrPatient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("facility")
    private Facility facility;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_event_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("vaccinationEvent")
    private VaccinationEvent vaccinationEvent;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "feedback_locations", joinColumns = @JoinColumn(name = "feedback_id"))
    @OrderBy("segmentId")
    private Set<Hl7Location> hl7Locations = new HashSet<>();

//    @JsonProperty("patient")
//    public void setPatient(int id) {
//        // is currently taken care of in the controller
//    }


//    @JsonProperty("facility")
//    public void setFacility(int id) {
//        // is currently taken care of in the controller
//    }

//    @JsonProperty("vaccinationEvent")
//    public void setVaccinationEvent(int id) {
//        // is currently taken care of in the controller
//    }

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "iis", length = 45)
    private String iis;

    @Column(name = "severity", length = 45)
    private String severity;

    @Column(name = "code", length = 45)
    private String code;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    @Column(name = "raw", columnDefinition = "TEXT")
    private String raw;

    public String getIis() {
        return iis;
    }

    public void setIis(String iis) {
        this.iis = iis;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public VaccinationEvent getVaccinationEvent() {
        return vaccinationEvent;
    }

    public void setVaccinationEvent(VaccinationEvent vaccinationEvent) {
        this.vaccinationEvent = vaccinationEvent;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Set<Hl7Location> getHl7Locations() {
        return hl7Locations;
    }

    public void setHl7Locations(Set<Hl7Location> hl7Locations) {
        this.hl7Locations = hl7Locations;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }
}