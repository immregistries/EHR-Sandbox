package org.immregistries.ehr.model;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static VaccinationEvent random(Patient patient, Facility facility){
        VaccinationEvent vacc_ev = new VaccinationEvent();
        vacc_ev.setPatient(patient);
        Vaccine vaccine = Vaccine.random();
        vacc_ev.setVaccine(vaccine);

        Clinician admicli = Clinician.random();
        Clinician ordercli = Clinician.random();
        Clinician entercli = Clinician.random();

        vacc_ev.setAdministeringFacility(facility);
        vacc_ev.setPatient(patient);
        vacc_ev.setEnteringClinician(entercli);
        vacc_ev.setOrderingClinician(ordercli);
        vacc_ev.setAdministeringClinician(admicli);
        vacc_ev.setVaccine(vaccine);
        return vacc_ev;
    }

}