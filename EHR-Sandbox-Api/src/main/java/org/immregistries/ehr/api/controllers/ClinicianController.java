package org.immregistries.ehr.api.controllers;


import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.logic.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/clinicians", "/tenants/{tenantId}/clinicians"})
public class ClinicianController {

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RandomGenerator randomGenerator;

    @GetMapping()
    public Iterable<Clinician> clinicians(@PathVariable Integer tenantId) {
        return  clinicianRepository.findByTenantId(tenantId);
    }

    @GetMapping("/{clinicianId}")
    public Optional<Clinician> clinician(@PathVariable Integer clinicianId) {
        return  clinicianRepository.findById(clinicianId);
    }

    @GetMapping("/$random")
    public Clinician random(@RequestAttribute Tenant tenant) {
        return  randomGenerator.randomClinician(tenant);
    }


    @PostMapping()
    public Clinician postClinicians(@RequestAttribute Tenant tenant, @RequestBody Clinician clinician) {
        clinician.setTenant(tenant);
        return clinicianRepository.save(clinician);
    }

    @PutMapping("/{clinicianId}")
    public Clinician putClinicians(@PathVariable Integer tenantId, @PathVariable Integer clinicianId, @RequestBody Clinician clinician) {
        Optional<Clinician> old = clinicianRepository.findByTenantIdAndId(tenantId,clinicianId);
        clinician.setTenant(old.get().getTenant());
        return clinicianRepository.save(clinician);
    }
}
