package org.immregistries.ehr.api.controllers;

import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
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
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients"})
public class PatientController {

    @Autowired
    private EhrPatientRepository patientRepository;
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
    public Iterable<EhrPatient> patients(@PathVariable() int tenantId,
                                         @PathVariable() int facilityId) {
        return  patientRepository.findByTenantIdAndFacilityId(tenantId,facilityId);
    }

    @GetMapping("/{patientId}")
    public Optional<EhrPatient> patient(@PathVariable() int tenantId,
                                        @PathVariable() int facilityId,
                                        @PathVariable() String patientId) {
        return  patientRepository.findById(patientId);
    }


    @PostMapping()
    public ResponseEntity<String> postPatient(@PathVariable() int tenantId,
                                               @PathVariable() int facilityId,
                                               @RequestBody EhrPatient patient) {
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
        EhrPatient newEntity = patientRepository.save(patient);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        return ResponseEntity.created(location).build();
//        return ResponseEntity.created(location).body("Patient " + newEntity.getId() + " saved");
    }

    @PutMapping("")
    public EhrPatient putPatient(@PathVariable() int tenantId,
                                 @PathVariable() int facilityId,
                                 @RequestBody EhrPatient patient) {
        // patient data check + flavours
        Optional<Tenant> tenant = tenantRepository.findById(tenantId);
        Optional<Facility> facility = facilityRepository.findById(facilityId);
        Optional<EhrPatient> oldPatient = patientRepository.findByFacilityIdAndId(facilityId,patient.getId());
        if (!tenant.isPresent() || !facility.isPresent() || !oldPatient.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid ids");
        }
        patient.setTenant(tenant.get());
        patient.setFacility(facility.get());
        patient.setUpdatedDate(new Date());
        EhrPatient newEntity = patientRepository.save(patient);
        return newEntity;
//        return ResponseEntity.created(location).body("Patient " + newEntity.getId() + " saved");
    }



}
