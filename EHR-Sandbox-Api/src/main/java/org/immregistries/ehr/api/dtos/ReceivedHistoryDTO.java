package org.immregistries.ehr.api.dtos;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.VaccinationEvent;

import java.util.List;


public class ReceivedHistoryDTO {
    EhrPatient patient;
    List<VaccinationEvent> vaccinationEvents;

    public EhrPatient getPatient() {
        return patient;
    }

    public void setPatient(EhrPatient patient) {
        this.patient = patient;
    }

    public List<VaccinationEvent> getVaccinationEvents() {
        return vaccinationEvents;
    }

    public void setVaccinationEvents(List<VaccinationEvent> vaccinationEvents) {
        this.vaccinationEvents = vaccinationEvents;
    }
}
