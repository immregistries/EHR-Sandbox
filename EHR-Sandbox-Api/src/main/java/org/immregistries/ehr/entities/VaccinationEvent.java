package org.immregistries.ehr.entities;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
@Table(name = "vaccination_event")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class VaccinationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccination_event_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
//    @JsonBackReference("patient-vaccinationEvent")
    @JsonIgnore
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entering_clinician_id")
//    @JsonBackReference("enteringClinician")
    private Clinician enteringClinician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_clinician_id")
//    @JsonBackReference("orderingClinician")
    private Clinician orderingClinician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administering_clinician_id")
//    @JsonBackReference("administeringClinician")
    private Clinician administeringClinician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id")
    private Vaccine vaccine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administering_facility_id", nullable = false)
//    @JsonBackReference("facility-vaccinationEvent")
    @JsonIgnore
    private Facility administeringFacility;

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