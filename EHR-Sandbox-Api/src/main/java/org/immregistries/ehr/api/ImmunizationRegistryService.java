package org.immregistries.ehr.api;

import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ImmunizationRegistryService {
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Encapsulates security checks
     *
     * @param id
     * @return
     */
    public ImmunizationRegistry getImmunizationRegistry(Integer id) {
        Optional<ImmunizationRegistry> immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(id, userDetailsService.currentUserId());
        if (immunizationRegistry.isPresent()) {
            return immunizationRegistry.get();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }
}
