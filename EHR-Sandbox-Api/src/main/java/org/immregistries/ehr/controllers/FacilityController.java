package org.immregistries.ehr.controllers;

import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.Tenant;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.FeedbackRepository;
import org.immregistries.ehr.repositories.TenantRepository;
import org.immregistries.ehr.repositories.VaccinationEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/tenants/{tenantId}/facilities")
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
                                                  @PathVariable() int vaccinationId) {
        return vaccinationEventRepository.findByAdministeringFacilityIdAndId(facilityId,vaccinationId);
    }

    @PostMapping()
    public ResponseEntity<String> postFacility(@PathVariable() int tenantId,
                                               @RequestBody Facility facility) {
        if (facility.getNameDisplay().length() < 1){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No facility name specified");
        }else {
            Optional<Tenant> tenant = tenantRepository.findById(tenantId);
            if (!tenant.isPresent()){
                throw new ResponseStatusException(
                        HttpStatus.NOT_ACCEPTABLE, "Invalid tenant id");
            }
            if (facilityRepository.existsByTenantIdAndNameDisplay(tenantId, facility.getNameDisplay())){
                throw new ResponseStatusException(
                        HttpStatus.NOT_ACCEPTABLE, "Facility already exists");
            }
            facility.setTenant(tenant.get());
            Facility newEntity = facilityRepository.save(facility);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newEntity.getId())
                    .toUri();
            return ResponseEntity.created(location).build();
//            return ResponseEntity.created(location).body("Facility " + newEntity.getId() + " saved");
        }
    }
}
