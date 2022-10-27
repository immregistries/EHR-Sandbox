package org.immregistries.ehr.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r5.model.Identifier;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.Tenant;
import org.immregistries.ehr.logic.PatientHandler;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.ResourceClient;
import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.repositories.PatientRepository;
import org.immregistries.ehr.repositories.TenantRepository;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import java.net.URI;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients"})
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    RandomGenerator randomGenerator;

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);


    @GetMapping()
    public Iterable<Patient> patients(@PathVariable() int tenantId,
                                      @PathVariable() int facilityId) {
        return  patientRepository.findByTenantIdAndFacilityId(tenantId,facilityId);
    }

    @GetMapping("/{patientId}")
    public Optional<Patient> patient(@PathVariable() int tenantId,
                                     @PathVariable() int facilityId,
                                     @PathVariable() int patientId) {
        return  patientRepository.findById(patientId);
    }

    @GetMapping("/random")
    public Patient random(@PathVariable() int tenantId,
                          @PathVariable() int facilityId) {
        Optional<Facility> facility = facilityRepository.findByIdAndTenantId(facilityId,tenantId);
        if (!facility.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid tenant id");
        }
        return randomGenerator.randomPatient(facility.get().getTenant(), facility.get());
    }

    @PostMapping()
    public ResponseEntity<String> postPatient(@PathVariable() int tenantId,
                                               @PathVariable() int facilityId,
                                               @RequestBody Patient patient) {
        // patient data check + flavours
        Optional<Tenant> tenant = tenantRepository.findById(tenantId);
        Optional<Facility> facility = facilityRepository.findById(facilityId);
        if (!tenant.isPresent() || !facility.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid tenant id or facilityId");
        }
        patient.setTenant(tenant.get());
        patient.setFacility(facility.get());
        patient.setCreatedDate(new Date());
        patient.setUpdatedDate(new Date());
        Patient newEntity = patientRepository.save(patient);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        return ResponseEntity.created(location).build();
//        return ResponseEntity.created(location).body("Patient " + newEntity.getId() + " saved");
    }

    @PutMapping("")
    public Patient putPatient(@PathVariable() int tenantId,
                              @PathVariable() int facilityId,
                              @RequestBody Patient patient) {
        // patient data check + flavours
        Optional<Tenant> tenant = tenantRepository.findById(tenantId);
        Optional<Facility> facility = facilityRepository.findById(facilityId);
        Optional<Patient> oldPatient = patientRepository.findByFacilityIdAndId(facilityId,patient.getId());
        if (!tenant.isPresent() || !facility.isPresent() || !oldPatient.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid ids");
        }
        patient.setTenant(tenant.get());
        patient.setFacility(facility.get());
        patient.setUpdatedDate(new Date());
        Patient newEntity = patientRepository.save(patient);
        return newEntity;
//        return ResponseEntity.created(location).body("Patient " + newEntity.getId() + " saved");
    }



}
