package org.immregistries.ehr.model;
import java.io.Serializable;

public class VaccinationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private int vaccinationEventId = 0;
    private Clinician administeringClinician = null;
    private Clinician enteringClinician = null;
    private Clinician orderingClinician = null;
    private Facility administeringFacility = null;
    private Patient patient = null;
    private Vaccine vaccine = null;

    public int getVaccinationEventId() {
        return vaccinationEventId;
    }

    public void setVaccinationEventId(int vaccinationEventId) {
        this.vaccinationEventId = vaccinationEventId;
    }

    public Clinician getAdministeringClinician() {
        return administeringClinician;
    }

    public void setAdministeringClinician(Clinician administeringClinician) {
        this.administeringClinician = administeringClinician;
    }

    public Clinician getEnteringClinician() {
        return enteringClinician;
    }

    public void setEnteringClinician(Clinician enteringClinician) {
        this.enteringClinician = enteringClinician;
    }

    public Clinician getOrderingClinician() {
        return orderingClinician;
    }

    public void setOrderingClinician(Clinician orderingClinician) {
        this.orderingClinician = orderingClinician;
    }

    public Facility getAdministeringFacility() {
        return administeringFacility;
    }

    public void setAdministeringFacility(Facility administeringFacility) {
        this.administeringFacility = administeringFacility;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Vaccine getVaccine() {
        return vaccine;
    }

    public void setVaccine(Vaccine vaccine) {
        this.vaccine = vaccine;
    }

}