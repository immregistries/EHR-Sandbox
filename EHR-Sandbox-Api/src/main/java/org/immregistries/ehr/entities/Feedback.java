package org.immregistries.ehr.entities;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "feedback"
//        , indexes = {
//        @Index(name = "fk_vaccination_event_idx", columnList = "vaccination_event_id"),
//        @Index(name = "fk_facility_idx", columnList = "facility_id"),
//        @Index(name = "patient_id_idx", columnList = "patient_id")
//}
)
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class,
        property="id",
        scope = Feedback.class)
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("patient")
    private Patient patient;

    @JsonProperty("patient")
    public void setPatient(int id) {
        // TODO is currently taken care of in the controller
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("facility")
    private Facility facility;

    @JsonProperty("facility")
    public void setFacility(int id) {
        // TODO is currently taken care of in the controller
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_event_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("vaccinationEvent")
    private VaccinationEvent vaccinationEvent;

    @JsonProperty("vaccinationEvent")
    public void setVaccinationEvent(int id) {
        // TODO is currently taken care of in the controller
    }

    @Column(name = "content")
    private String content;

    @Column(name = "iis", length = 45)
    private String iis;

    @Column(name = "severity", length = 45)
    private String severity;

    @Column(name = "code", length = 45)
    private String code;

    @JoinColumn(name = "date", nullable = false)
    @JsonIgnore
    private Date date;

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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}