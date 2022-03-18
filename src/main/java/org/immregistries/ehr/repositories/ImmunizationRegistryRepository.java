package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.springframework.data.repository.CrudRepository;

public interface ImmunizationRegistryRepository extends CrudRepository<ImmunizationRegistry, Integer> {
}