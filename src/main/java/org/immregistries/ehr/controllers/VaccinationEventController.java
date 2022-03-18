package org.immregistries.ehr.controllers;


import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.Tenant;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.PatientRepository;
import org.immregistries.ehr.repositories.TenantRepository;
import org.immregistries.ehr.repositories.VaccinationEventRepository;
import org.immregistries.ehr.security.AuthTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations"})
public class VaccinationEventController {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;



    @GetMapping()
    public Iterable<VaccinationEvent> vaccinationEvents(@PathVariable() int patientId) {
        return vaccinationEventRepository.findByPatientId(patientId);
    }

    @GetMapping("/{vaccinationId}")
    public Optional<VaccinationEvent> vaccinationEvent(@PathVariable() int vaccinationId) {
        return  vaccinationEventRepository.findById(vaccinationId);
    }
}
