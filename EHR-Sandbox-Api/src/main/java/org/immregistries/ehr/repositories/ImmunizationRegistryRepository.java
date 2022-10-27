package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ImmunizationRegistryRepository extends CrudRepository<ImmunizationRegistry, Integer> {
    Iterable<ImmunizationRegistry> findByUserId(Integer userId);
    Optional<ImmunizationRegistry> findByIdAndUserId(Integer id, Integer userId);
    Boolean existsByNameAndUserId(String name, Integer userId);
}