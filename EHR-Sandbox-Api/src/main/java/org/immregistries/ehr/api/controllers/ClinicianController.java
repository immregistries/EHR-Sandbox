package org.immregistries.ehr.api.controllers;


import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.logic.RandomGeneratorService;
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
    private RandomGeneratorService randomGeneratorService;

    @GetMapping()
    public Iterable<Clinician> clinicians(@PathVariable(TENANT_ID) Integer tenantId) {
        return clinicianRepository.findByTenantId(tenantId);
    }

    @GetMapping(CLINICIAN_ID_SUFFIX)
    public Optional<Clinician> clinician(@PathVariable(CLINICIAN_ID) Integer clinicianId) {
        return clinicianRepository.findById(clinicianId);
    }

    @GetMapping("/$random")
    public Clinician random(@PathVariable(TENANT_ID) Integer tenantId) {
        return randomGeneratorService.randomClinician(tenantId);
    }


    @PostMapping()
    public Clinician postClinicians(@PathVariable(TENANT_ID) Integer tenantId, @RequestBody Clinician clinician) {
        return postClinicians(tenantRepository.findById(tenantId).get(), clinician);
    }

    public Clinician postClinicians(Tenant tenant, Clinician clinician) {
        if (clinician.getId() != null && clinician.getId() > -1) {
            Optional<Clinician> old = clinicianRepository.findByTenantAndId(tenant, clinician.getId());
            if (old.isEmpty()) {
                clinician.setId(null);
            }
        }
        clinician.setTenant(tenant);

        return clinicianRepository.save(clinician);
    }

    @PutMapping(CLINICIAN_ID_SUFFIX)
    public Clinician putClinicians(@PathVariable(TENANT_ID) Integer tenantId, @PathVariable(CLINICIAN_ID) Integer clinicianId, @RequestBody Clinician clinician) {
        Optional<Clinician> old = clinicianRepository.findByTenantIdAndId(tenantId, clinicianId);
        clinician.setTenant(old.get().getTenant());
        return clinicianRepository.save(clinician);
    }
}
