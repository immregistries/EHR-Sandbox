package org.immregistries.ehr.controllers;


import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.repositories.ClinicianRepository;
import org.immregistries.ehr.repositories.PatientRepository;
import org.immregistries.ehr.repositories.VaccinationEventRepository;
import org.immregistries.ehr.repositories.VaccineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations"})
public class VaccinationController {

    @Autowired
    VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    ClinicianRepository clinicianRepository;
    @Autowired
    VaccineRepository vaccineRepository;



    @GetMapping()
    public Iterable<VaccinationEvent> vaccinationEvents(@PathVariable() int patientId) {
        return vaccinationEventRepository.findByPatientId(patientId);
    }

    @GetMapping("/{vaccinationId}")
    public Optional<VaccinationEvent> vaccinationEvent(@PathVariable() int vaccinationId) {
        return  vaccinationEventRepository.findById(vaccinationId);
    }

    @GetMapping("/random")
    public VaccinationEvent random( @PathVariable() int facilityId,
                                              @PathVariable() int patientId) {
        Optional<Patient> patient = patientRepository.findByFacilityIdAndId(facilityId,patientId);
        if (!patient.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid patient id");
        }
        return RandomGenerator.randomVaccinationEvent(patient.get(), patient.get().getFacility());
    }

    @PostMapping()
    public ResponseEntity<String> postVaccinationEvents(@PathVariable() int patientId,
                                                        @RequestBody VaccinationEvent vaccination) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (!patient.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No patient found");
        }
        vaccination.setAdministeringClinician(clinicianRepository.save(vaccination.getAdministeringClinician()));
        vaccination.setOrderingClinician(clinicianRepository.save(vaccination.getOrderingClinician()));
        vaccination.setEnteringClinician(clinicianRepository.save(vaccination.getEnteringClinician()));
        vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
        vaccination.setPatient(patient.get());
        vaccination.setAdministeringFacility(patient.get().getFacility());
        VaccinationEvent newEntity = vaccinationEventRepository.save(vaccination);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }


    @PutMapping()
    public VaccinationEvent putVaccinationEvents(@PathVariable() int patientId,
                                                 @RequestBody VaccinationEvent vaccination) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        Optional<VaccinationEvent> oldVaccination =  vaccinationEventRepository.findByPatientIdAndId(patientId, vaccination.getId());
        if (!patient.isPresent() || !oldVaccination.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No patient found");
        }
        vaccination.setAdministeringClinician(clinicianRepository.save(vaccination.getAdministeringClinician()));
        vaccination.setOrderingClinician(clinicianRepository.save(vaccination.getOrderingClinician()));
        vaccination.setEnteringClinician(clinicianRepository.save(vaccination.getEnteringClinician()));
        vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
        vaccination.setPatient(patient.get());
        vaccination.setAdministeringFacility(patient.get().getFacility());
        VaccinationEvent newEntity = vaccinationEventRepository.save(vaccination);
        return newEntity;
    }



}
