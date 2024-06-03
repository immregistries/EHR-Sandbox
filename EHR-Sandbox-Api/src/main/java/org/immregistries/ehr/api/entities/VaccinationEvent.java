package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "vaccination_event")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = VaccinationEvent.class)
@Audited
public class VaccinationEvent extends EhrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccination_event_id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("patient")
    private EhrPatient patient;

    /**
     * DO NOT USE THIS METHOD
     *
     * @param id
     */
    @JsonProperty("patient")
    public void setPatient(String id) {
        // TODO is currently taken care of in the controller (Problem is I can't make repositories accessible in Entity definition)
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entering_clinician_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    private Clinician enteringClinician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_clinician_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    private Clinician orderingClinician;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "administering_clinician_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    private Clinician administeringClinician;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vaccine_id")
    private Vaccine vaccine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administering_facility_id", nullable = false)
    @JsonBackReference("facility-vaccinationEvent")
    @Audited(targetAuditMode = NOT_AUDITED)
    private Facility administeringFacility;

    @Column(name = "primary_source")
    private boolean primarySource;
    @OneToMany(mappedBy = "vaccinationEvent")
//    @JsonDeserialize(using = CustomFeedbackListDeserializer.class)
    @NotAudited
    private Set<Feedback> feedbacks = new LinkedHashSet<>();

    public Set<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(Set<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Facility getAdministeringFacility() {
        return administeringFacility;
    }

    public void setAdministeringFacility(Facility administeringFacility) {
        this.administeringFacility = administeringFacility;
    }

    public Vaccine getVaccine() {
        return vaccine;
    }

    public void setVaccine(Vaccine vaccine) {
        this.vaccine = vaccine;
    }

    public Clinician getAdministeringClinician() {
        return administeringClinician;
    }

    public void setAdministeringClinician(Clinician administeringClinician) {
        this.administeringClinician = administeringClinician;
    }

    public Clinician getOrderingClinician() {
        return orderingClinician;
    }

    public void setOrderingClinician(Clinician orderingClinician) {
        this.orderingClinician = orderingClinician;
    }

    public Clinician getEnteringClinician() {
        return enteringClinician;
    }

    public void setEnteringClinician(Clinician enteringClinician) {
        this.enteringClinician = enteringClinician;
    }

//    @JsonProperty("patientId")
//    @Transient()
//    public String getPatientId() {
//        if (patient == null) {
//            return null;
//        }
//        return patient.getId();
//    }

    public EhrPatient getPatient() {
        return patient;
    }

    public void setPatient(EhrPatient patient) {
        this.patient = patient;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPrimarySource() {
        return primarySource;
    }

    public void setPrimarySource(boolean primarySource) {
        this.primarySource = primarySource;
    }

}