package org.immregistries.ehr.entities;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "vaccination_event")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class,
        property="id",
        scope = VaccinationEvent.class)
public class VaccinationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccination_event_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("patient")
    private Patient patient;

    @JsonProperty("patient")
    public void setPatient(int id) {
        // TODO is currently taken care of in the controller (Problem is I can't make repositories accessible in Entity definition)
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entering_clinician_id")
    private Clinician enteringClinician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_clinician_id")
    private Clinician orderingClinician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administering_clinician_id")
    private Clinician administeringClinician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id")
    private Vaccine vaccine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administering_facility_id", nullable = false)
    @JsonBackReference("facility-vaccinationEvent")
    private Facility administeringFacility;

    @OneToMany(mappedBy = "vaccinationEvent")
//    @JsonDeserialize(using = CustomFeedbackListDeserializer.class)
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