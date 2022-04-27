package org.immregistries.ehr.entities;

import com.fasterxml.jackson.annotation.*;
import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;

import javax.persistence.*;

@Entity
@Table(name = "feedback"
//        , indexes = {
//        @Index(name = "fk_vaccination_event_idx", columnList = "vaccination_event_id"),
//        @Index(name = "fk_facility_idx", columnList = "facility_id"),
//        @Index(name = "patient_id_idx", columnList = "patient_id")
//}
)
//@JsonIdentityInfo(generator= ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference(value = "patient-feedback")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    @JsonBackReference(value = "facility-feedback")
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_event_id")
    @JsonBackReference(value = "vaccination-feedback")
    private VaccinationEvent vaccinationEvent;

    @Column(name = "content")
    private String content;

    @Column(name = "iis", length = 45)
    private String iis;

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
}