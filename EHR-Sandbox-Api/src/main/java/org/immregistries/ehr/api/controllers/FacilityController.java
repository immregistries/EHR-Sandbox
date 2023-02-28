package org.immregistries.ehr.api.controllers;

import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.FeedbackRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities"})
public class FacilityController {

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;

    @GetMapping()
    public Iterable<Facility> getFacilities(@PathVariable() int tenantId) {
        return facilityRepository.findByTenantId(tenantId);
    }

    @GetMapping("/{facilityId}")
    public Optional<Facility> getFacility(@PathVariable() int tenantId,
                                          @PathVariable() int facilityId) {
        return facilityRepository.findByIdAndTenantId(facilityId,tenantId);
    }

    @GetMapping("/{facilityId}/vaccinations/{vaccinationId}")
    public Optional<VaccinationEvent> getVaccination(@PathVariable() int tenantId,
                                                  @PathVariable() int facilityId,
                                                  @PathVariable() String vaccinationId) {
        return vaccinationEventRepository.findByAdministeringFacilityIdAndId(facilityId,vaccinationId);
    }

    @PostMapping()
    public ResponseEntity<Facility> postFacility(@PathVariable() int tenantId,
                                               @RequestBody Facility facility) {
        if (facility.getNameDisplay().length() < 1){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No facility name specified");
        }else {
            if (facilityRepository.existsByTenantIdAndNameDisplay(tenantId, facility.getNameDisplay())){
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Facility already exists");
            }
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid tenant id"));
            facility.setTenant(tenant);
            Facility newEntity = facilityRepository.save(facility);
            return new ResponseEntity<>(newEntity, HttpStatus.CREATED);
        }
    }
}
