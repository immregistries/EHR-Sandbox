package org.immregistries.ehr.controllers;


import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.Tenant;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.repositories.PatientRepository;
import org.immregistries.ehr.repositories.VaccinationEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Optional;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations"})
public class VaccinationController {

    @Autowired
    VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    PatientRepository patientRepository;

    @GetMapping("/{vaccinationEventId}")
    public Optional<VaccinationEvent> vaccinationEvent(@PathVariable() int patientId,
                                                       @PathVariable() int vaccinationEventId){
        return vaccinationEventRepository.findByPatientIdAndId(patientId, vaccinationEventId);
    }

    @GetMapping()
    public Iterable<VaccinationEvent> getVaccinationEvents(@PathVariable() int patientId){
        return vaccinationEventRepository.findByPatientId(patientId);
    }

    @PostMapping()
    public @ResponseBody String postVaccinationEvents(@PathVariable() int patientId,
                                                       @RequestBody VaccinationEvent vaccinationEvent) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (!patient.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No facility name specified");
        }
        vaccinationEvent.setPatient(patient.get());
        VaccinationEvent newVaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
        return "Vaccination " + newVaccinationEvent.getId() + " created";
    }



}
