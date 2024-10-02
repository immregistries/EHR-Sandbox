package org.immregistries.ehr.api.controllers;

import com.github.javafaker.Faker;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
@RequestMapping(TENANT_PATH)
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping("/$random")
    public Tenant getRandom() {
        Faker faker = new Faker();
        Tenant tenant = new Tenant();
        tenant.setUser(userDetailsService.currentUser());
        tenant.setNameDisplay(faker.animal().name());
        return tenant;
    }

    @GetMapping()
    public Iterable<Tenant> tenants() {
        return tenantRepository.findByUserId(userDetailsService.currentUserId());
    }

    @GetMapping(TENANT_ID_SUFFIX)
    public Optional<Tenant> getTenant(@PathVariable(TENANT_ID) Integer tenantId) {
        return tenantRepository.findById(tenantId);
    }

    @GetMapping(TENANT_ID_SUFFIX + PATIENT_PATH_HEADER)
    public Iterable<EhrPatient> patients(@PathVariable(TENANT_ID) Integer tenantId) {
        return patientRepository.findByTenantId(tenantRepository.findById(tenantId).orElseThrow());
    }

    @PostMapping()
    public ResponseEntity<Tenant> postTenant(@RequestBody Tenant tenant) {
        if (tenantRepository.existsByUserIdAndNameDisplay(userDetailsService.currentUserId(), tenant.getNameDisplay())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Tenant already exists");
        }
        if (tenant.getNameDisplay().length() < 1) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No tenant name specified");
        }
        tenant.setUser(userDetailsService.currentUser());
        Tenant newTenant = tenantRepository.save(tenant);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTenant.getId())
                .toUri();
        return new ResponseEntity<>(newTenant, HttpStatus.CREATED);
    }


}
