package org.immregistries.ehr.api.controllers;


import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
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

    @GetMapping()
    public Iterable<Clinician> clinicians(@PathVariable Integer tenantId) {
        return  clinicianRepository.findByTenantId(tenantId);
    }

    @GetMapping("/{clinicianId}")
    public Optional<Clinician> clinician(@PathVariable Integer clinicianId) {
        return  clinicianRepository.findById(clinicianId);
    }


    @PostMapping()
    public Clinician postClinicians(@PathVariable Integer tenantId, @RequestBody Clinician clinician) {
        Tenant tenant = tenantRepository.findById(tenantId).get();
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
