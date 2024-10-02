package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.github.javafaker.Faker;
import jakarta.transaction.Transactional;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.repositories.AuditRevisionEntityRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.logic.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
@RequestMapping({FACILITY_PATH})
public class FacilityController {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private EhrPatientController ehrPatientController;
    @Autowired
    private RandomGenerator randomGenerator;
    @Autowired
    private AuditRevisionEntityRepository auditRevisionEntityRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping()
    public Iterable<Facility> getFacilities(@PathVariable(TENANT_ID) Integer tenantId) {
        return facilityRepository.findByTenantId(tenantId);
    }

    @GetMapping("/$random")
    public Facility getRandom(@PathVariable(TENANT_ID) Integer tenantId) {
        Faker faker = new Faker();
        Facility facility = new Facility();
        facility.setTenant(tenantRepository.findById(tenantId).get());
        facility.setNameDisplay(faker.company().name());
        return facility;
    }

    @GetMapping(FACILITY_ID_SUFFIX + "/$random_patient")
    public EhrPatient getRandomPatient(@PathVariable(FACILITY_ID) Integer facilityId) {
        return randomGenerator.randomPatient(facilityRepository.findById(facilityId).get());
    }

    @GetMapping(FACILITY_ID_SUFFIX)
    public Optional<Facility> getFacility(@PathVariable(TENANT_ID) Integer tenantId,
                                          @PathVariable(FACILITY_ID) Integer facilityId) {
        return facilityRepository.findByIdAndTenantId(facilityId, tenantId);
    }

    @GetMapping(FACILITY_ID_SUFFIX + "/$children")
    public Set<Facility> getFacilityChildren(@PathVariable(TENANT_ID) Integer tenantId, @PathVariable(FACILITY_ID) Integer facilityId) {
        return facilityRepository.findByIdAndTenantId(facilityId, tenantId).orElseThrow().getFacilities();
    }

    @PostMapping()
    public ResponseEntity<Facility> postFacility(@PathVariable(TENANT_ID) Integer tenantId, @RequestBody Facility facility, @RequestParam("populate") Optional<Boolean> populate) {
        return postFacility(tenantRepository.findById(tenantId).get(), facility, populate);
    }

    public ResponseEntity<Facility> postFacility(Tenant tenant, Facility facility, Optional<Boolean> populate) {
        if (facility.getNameDisplay().length() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No facility name specified");
        }
        if (facilityRepository.existsByTenantIdAndNameDisplay(tenant.getId(), facility.getNameDisplay())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Name is already used");
        }
        if (facility.getParentFacility() != null) {
            Facility parentFacility = facilityRepository.findByIdAndTenantId(facility.getParentFacility().getId(), tenant.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid parent facility, must have same tenant"));
            /**
             * Making sure facility is not included in prentFacility hierarchy
             */
            Facility parent = parentFacility;
            while (parent != null) {
                if (Objects.equals(parent.getId(), facility.getId())) {
                    throw new InvalidRequestException("Impossible Parent Facility");
                }
                parent = parent.getParentFacility();
            }

            facility.setParentFacility(parentFacility);
        }
        facility.setTenant(tenant);
        Facility newEntity = facilityRepository.save(facility);
        if (populate.isPresent()) {
            populateFacility(tenant, facility, Optional.of(3));
        }
        return new ResponseEntity<>(newEntity, HttpStatus.CREATED);
    }

    @PutMapping()
    public ResponseEntity<Facility> putFacility(@PathVariable(TENANT_ID) Integer tenantId, @RequestBody Facility facility) {
        if (facility.getNameDisplay().length() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No facility name specified");
        }
        Optional<Facility> oldWithSameName = facilityRepository.findByTenantIdAndNameDisplay(tenantId, facility.getNameDisplay());
        if (oldWithSameName.isPresent() && !Objects.equals(oldWithSameName.get().getId(), facility.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Name is already used");
        }
        Facility oldFacility = facilityRepository.findByIdAndTenantId(facility.getId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid put facility, must have same tenant"));
        facility.setTenant(oldFacility.getTenant());
        Facility newEntity = facilityRepository.save(facility);
        return new ResponseEntity<>(newEntity, HttpStatus.CREATED);
    }

    @GetMapping(FACILITY_ID_SUFFIX + "/$populate")
    @Transactional()
    public ResponseEntity<String> populateFacility(
            @PathVariable(TENANT_ID) Integer tenantId,
            @PathVariable(FACILITY_ID) Integer facilityId,
            @RequestParam("patientNumber") Optional<Integer> patientNumber) {
        return populateFacility(tenantRepository.findById(tenantId).get(), facilityRepository.findById(facilityId).get(), patientNumber);
    }

    public ResponseEntity<String> populateFacility(
            Tenant tenant,
            Facility facility,
            Optional<Integer> patientNumber) {
        if (patientNumber.isEmpty()) {
            patientNumber = Optional.of(3);
        } else if (patientNumber.get() > 30) {
            patientNumber = Optional.of(3);
        }
        for (int i = 0; i < patientNumber.get(); i++) {
            ehrPatientController.postPatient(tenant, facility, randomGenerator.randomPatient(facility), Optional.of(true));
        }
        return ResponseEntity.ok("{}");
    }

    /**
     * Used by frontend to check if a refresh is needed on the current facility it is displaying
     *
     * @return
     */
    @GetMapping("/$notification")
    public Boolean notificationCheck(@RequestParam("timestamp") Optional<Long> timestamp,
                                     @PathVariable(FACILITY_ID) Integer facilityId) {
        return auditRevisionEntityRepository.existsByUserAndTimestampGreaterThanAndSubscriptionIdNotNull(
                userDetailsService.currentUserId(),
                timestamp.orElse(0L)); // TODO add facility to audit revision
    }
}
