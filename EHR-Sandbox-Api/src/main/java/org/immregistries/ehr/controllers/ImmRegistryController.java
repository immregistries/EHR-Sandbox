package org.immregistries.ehr.controllers;

import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class ImmRegistryController {
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping({"/settings/{id}","/imm-registry/{id}"})
    public ImmunizationRegistry settings(@PathVariable Integer id) {
        Optional<ImmunizationRegistry> immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(id,userDetailsService.currentUserId());
        if (immunizationRegistry.isPresent()){
            return immunizationRegistry.get();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    @GetMapping({"/settings","/imm-registry"})
    public Iterable<ImmunizationRegistry> getImmRegistries() {
        return immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
    }

    @PutMapping({"/settings","/imm-registry"})
    public ImmunizationRegistry putSettings(@RequestBody ImmunizationRegistry settings) {
        Optional<ImmunizationRegistry> oldSettings = immunizationRegistryRepository.findByIdAndUserId(settings.getId(),userDetailsService.currentUserId());
        if (oldSettings.isPresent()){
            settings.setUser(userDetailsService.currentUser());
            return immunizationRegistryRepository.save(settings);
        } else {
            return postSettings(settings);
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    @PostMapping({"/settings","/imm-registry"})
    public ImmunizationRegistry postSettings(@RequestBody ImmunizationRegistry settings) {
        settings.setUser(userDetailsService.currentUser());
        settings.setId(null);
        if(immunizationRegistryRepository.existsByNameAndUserId(settings.getName(),userDetailsService.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used");
        }
        return immunizationRegistryRepository.save(settings);
    }


}
