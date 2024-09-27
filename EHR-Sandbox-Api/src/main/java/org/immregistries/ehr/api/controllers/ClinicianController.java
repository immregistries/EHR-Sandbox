package org.immregistries.ehr.api.controllers;


import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.logic.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
@RequestMapping({FACILITY_ID_PATH + CLINICIAN_PATH_HEADER, ControllerHelper.CLINICIAN_PATH})
public class ClinicianController {
    Logger logger = LoggerFactory.getLogger(ClinicianController.class);

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RandomGenerator randomGenerator;

    @GetMapping()
    public Iterable<Clinician> clinicians(@PathVariable() String tenantId) {
        return clinicianRepository.findByTenantId(tenantId);
    }

    @GetMapping(CLINICIAN_ID_SUFFIX)
    public Optional<Clinician> clinician(@PathVariable() String clinicianId) {
        return clinicianRepository.findById(clinicianId);
    }

    @GetMapping("/$random")
    public Clinician random(@PathVariable() String tenantId) {
        return randomGenerator.randomClinician(tenantId);
    }


    @PostMapping()
    public Clinician postClinicians(@PathVariable() String tenantId, @RequestBody Clinician clinician) {
        return postClinicians(tenantRepository.findById(tenantId).get(), clinician);
    }

    public Clinician postClinicians(Tenant tenant, Clinician clinician) {
        clinician.setTenant(tenant);
        Clinician newClinician = clinicianRepository.save(clinician);
        return newClinician;
    }

    @PutMapping(CLINICIAN_ID_SUFFIX)
    public Clinician putClinicians(@PathVariable() String tenantId, @PathVariable() String clinicianId, @RequestBody Clinician clinician) {
        Optional<Clinician> old = clinicianRepository.findByTenantIdAndId(tenantId, clinicianId);
        clinician.setTenant(old.get().getTenant());
        return clinicianRepository.save(clinician);
    }
}
