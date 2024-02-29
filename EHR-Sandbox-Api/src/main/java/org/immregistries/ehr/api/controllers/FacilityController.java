package org.immregistries.ehr.api.controllers;

import com.github.javafaker.Faker;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities"})
public class FacilityController {

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private AuditRevisionEntityRepository auditRevisionEntityRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping()
    public Iterable<Facility> getFacilities(@PathVariable() String tenantId) {
        return facilityRepository.findByTenantId(tenantId);
    }

    @GetMapping("/$random")
    public Facility getRandom(@RequestAttribute() Tenant tenant) {
        Faker faker = new Faker();

        Facility facility = new Facility();
        facility.setTenant(tenant);
        facility.setNameDisplay(faker.company().name());
        return facility;
    }

    @GetMapping("/{facilityId}")
    public Optional<Facility> getFacility(@PathVariable() String tenantId,
                                          @PathVariable() String facilityId) {
        return facilityRepository.findByIdAndTenantId(facilityId,tenantId);
    }

    @PostMapping()
    public ResponseEntity<Facility> postFacility(@RequestAttribute Tenant tenant, @RequestBody Facility facility, @RequestParam Optional<String> parentId) {
        if (facility.getNameDisplay().length() < 1){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No facility name specified");
        }else {
            if (facilityRepository.existsByTenantIdAndNameDisplay(tenant.getId(), facility.getNameDisplay())){
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Facility already exists");
            }
            if (parentId.isPresent()) {
                Facility parentFacility = facilityRepository.findByIdAndTenantId(parentId.get(), tenant.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid parent facility, must have same tenant"));
                facility.setParentFacility(parentFacility);
            }
            facility.setTenant(tenant);
            Facility newEntity = facilityRepository.save(facility);
            return new ResponseEntity<>(newEntity, HttpStatus.CREATED);
        }
    }

    /**
     * Used by frontend to check if a refresh is needed on the current facility it is displaying
     * @return
     */
    @GetMapping("/$notification")
    public Boolean notificationCheck(@RequestParam Optional<Long> timestamp,
                                     @PathVariable() String facilityId) {
        return auditRevisionEntityRepository.existsByUserAndTimestampGreaterThanAndSubscriptionIdNotNull(
                userDetailsService.currentUserId(),
                timestamp.orElse(0L)); // TODO add facility to audit revision
    }
}
