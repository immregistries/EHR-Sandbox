package org.immregistries.ehr.api.controllers;


import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.logic.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/clinicians", "/tenants/{tenantId}/clinicians"})
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
    public Iterable<Clinician> clinicians(@PathVariable String tenantId) {
        return clinicianRepository.findByTenantId(tenantId);
    }

    @GetMapping("/{clinicianId}")
    public Optional<Clinician> clinician(@PathVariable Integer clinicianId) {
        return clinicianRepository.findById(clinicianId);
    }

    @GetMapping("/$random")
    public Clinician random(@RequestAttribute Tenant tenant) {
        return randomGenerator.randomClinician(tenant);
    }


    @PostMapping()
    public Clinician postClinicians(@RequestAttribute Tenant tenant, @RequestBody Clinician clinician) {
        clinician.setTenant(tenant);
        for (EhrIdentifier clinicianIdentifier : clinician.getIdentifiers()) {
//            clinicianIdentifier.setClinicianId(clinician.getId());
            logger.info("Clinician Identifier {}", clinicianIdentifier);
        }
        Clinician newClinician = clinicianRepository.save(clinician);
        return clinicianRepository.save(clinician);
    }

    @PutMapping("/{clinicianId}")
    public Clinician putClinicians(@PathVariable String tenantId, @PathVariable Integer clinicianId, @RequestBody Clinician clinician) {
        Optional<Clinician> old = clinicianRepository.findByTenantIdAndId(tenantId, clinicianId);
        clinician.setTenant(old.get().getTenant());
        return clinicianRepository.save(clinician);
    }
}
