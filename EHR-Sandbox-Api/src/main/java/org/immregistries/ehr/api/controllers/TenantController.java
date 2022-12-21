package org.immregistries.ehr.api.controllers;

import org.immregistries.ehr.api.repositories.PatientRepository;
import org.immregistries.ehr.api.entities.Patient;
import org.immregistries.ehr.api.entities.Tenant;
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

@RestController
@RequestMapping("/tenants")
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping()
    public Iterable<Tenant> tenants() {
        return tenantRepository.findByUserId(userDetailsService.currentUserId());
    }

    @GetMapping("/{tenantId}")
    public Optional<Tenant> getTenant(@PathVariable() int tenantId) {
        return tenantRepository.findById(tenantId);
    }

    @GetMapping("/{tenantId}/patients")
    public Iterable<Patient> patients(@PathVariable() int tenantId) {
        return patientRepository.findByTenantId(tenantId);
    }

    @PostMapping()
    public ResponseEntity<Tenant> postTenant(@RequestBody Tenant tenant) {
        if (tenantRepository.existsByUserIdAndNameDisplay(userDetailsService.currentUserId(), tenant.getNameDisplay())){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Tenant already exists");
        }
        if (tenant.getNameDisplay().length() < 1){
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
