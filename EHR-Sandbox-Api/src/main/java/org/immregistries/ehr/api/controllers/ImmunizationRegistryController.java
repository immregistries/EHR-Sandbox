package org.immregistries.ehr.api.controllers;

import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class ImmunizationRegistryController {
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping({"/registry/{id}"})
    public ImmunizationRegistry getImmunizationRegistry(@PathVariable Integer id) {
        Optional<ImmunizationRegistry> immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(id, userDetailsService.currentUserId());
        if (immunizationRegistry.isPresent()) {
            return immunizationRegistry.get();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    @GetMapping({"/registry"})
    public Iterable<ImmunizationRegistry> getImmRegistries() {
        return immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
    }

    @PutMapping({"/registry"})
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

    @PostMapping({"/registry"})
    public ImmunizationRegistry postImmunizationRegistry(@RequestBody ImmunizationRegistry immunizationRegistry) {
        immunizationRegistry.setUser(userDetailsService.currentUser());
        immunizationRegistry.setId(null);
        if (immunizationRegistryRepository.existsByNameAndUserId(immunizationRegistry.getName(), userDetailsService.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used");
        }
        return immunizationRegistryRepository.save(immunizationRegistry);
    }

    @DeleteMapping({"/registry/{id}"})
    public ResponseEntity removeImmunizationRegistry(@PathVariable Integer id) {
        immunizationRegistryRepository.deleteByIdAndUserId(id, userDetailsService.currentUserId());
        return ResponseEntity.ok().build();
    }


}
