package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/registry")
public class ImmunizationRegistryController {
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private CustomClientFactory customClientFactory;

    @GetMapping({"/{id}"})
    public ImmunizationRegistry getImmunizationRegistry(@PathVariable Integer id) {
        Optional<ImmunizationRegistry> immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(id, userDetailsService.currentUserId());
        if (immunizationRegistry.isPresent()) {
            return immunizationRegistry.get();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    @GetMapping({"/{id}/metadata"})
    public ResponseEntity<String> getImmunizationRegistryMetadata(@PathVariable Integer id) {
        IGenericClient client = customClientFactory.newGenericClient(id);
        CapabilityStatement capabilityStatement;
        try {
            capabilityStatement = client.capabilities().ofType(CapabilityStatement.class).prettyPrint().execute();
        } catch (ResourceVersionConflictException resourceVersionConflictException) {
            /**
             * Conflict might rise because iis is creating tenant, so we give it another try
             */
            capabilityStatement = client.capabilities().ofType(CapabilityStatement.class).prettyPrint().execute();
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
        return ResponseEntity.ok(client.getFhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(capabilityStatement));
    }

    @GetMapping()
    public Iterable<ImmunizationRegistry> getImmRegistries() {
        return immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
    }

    @PutMapping()
    public ImmunizationRegistry putImmunizationRegistry(@RequestBody ImmunizationRegistry immunizationRegistry) {
        Optional<ImmunizationRegistry> old = immunizationRegistryRepository.findByIdAndUserId(immunizationRegistry.getId(), userDetailsService.currentUserId());
        if (old.isPresent()) {
            immunizationRegistry.setUser(userDetailsService.currentUser());
            return immunizationRegistryRepository.save(immunizationRegistry);
        } else {
            return postImmunizationRegistry(immunizationRegistry);
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    @PostMapping()
    public ImmunizationRegistry postImmunizationRegistry(@RequestBody ImmunizationRegistry immunizationRegistry) {
        immunizationRegistry.setUser(userDetailsService.currentUser());
        immunizationRegistry.setId(null);
        if (immunizationRegistryRepository.existsByNameAndUserId(immunizationRegistry.getName(), userDetailsService.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used");
        }
        return immunizationRegistryRepository.save(immunizationRegistry);
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity removeImmunizationRegistry(@PathVariable Integer id) {
        immunizationRegistryRepository.deleteByIdAndUserId(id, userDetailsService.currentUserId());
        return ResponseEntity.ok().build();
    }


}
