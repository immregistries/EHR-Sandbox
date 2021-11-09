package org.immregistries.ehr.model
import java.io.Serializable;
import java.util.Date;

public class VaccinationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private int vaccinationEventId = 0;
    private Clinician administeringClinicianId = null;
    private Clinician enteringClinicianId = null;
    private Clinician orderingClinicianId = null;
    private Facility administeringFacilityId = null;
    private Patient patientId = null;
    private Vaccine vaccineId = null;
    private LogsOfModifications logId = null;

    public int getVaccinationEventId() {
        return vaccinationEventId;
    }

    public void setVaccinationEventId(int vaccinationEventId) {
        this.vaccinationEventId = vaccinationEventId;
    }

    public Clinician getAdministeringClinicianId() {
        return administeringClinicianId;
    }

    public void setAdministeringClinicianId(Clinician administeringClinicianId) {
        this.administeringClinicianId = administeringClinicianId;
    }

    public Clinician getEnteringClinicianId() {
        return enteringClinicianId;
    }

    public void setEnteringClinicianId(Clinician enteringClinicianId) {
        this.enteringClinicianId = enteringClinicianId;
    }

    public Clinician getOrderingClinicianId() {
        return orderingClinicianId;
    }

    public void setOrderingClinicianId(Clinician orderingClinicianId) {
        this.orderingClinicianId = orderingClinicianId;
    }

    public Facility getAdministeringFacilityId() {
        return administeringFacilityId;
    }

    public void setAdministeringFacilityId(Facility administeringFacilityId) {
        this.administeringFacilityId = administeringFacilityId;
    }

    public Patient getPatientId() {
        return patientId;
    }

    public void setPatientId(Patient patientId) {
        this.patientId = patientId;
    }

    public Vaccine getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Vaccine vaccineId) {
        this.vaccineId = vaccineId;
    }

    public LogsOfModifications getLogId() {
        return logId;
    }

    public void setLogId(LogsOfModifications logId) {
        this.logId = logId;
    }
}