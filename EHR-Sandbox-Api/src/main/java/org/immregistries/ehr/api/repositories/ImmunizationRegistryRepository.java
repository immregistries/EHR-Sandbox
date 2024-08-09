package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ImmunizationRegistryRepository extends CrudRepository<ImmunizationRegistry, String> {
    Iterable<ImmunizationRegistry> findByUserId(Integer userId);

    Optional<ImmunizationRegistry> findByUserIdAndIisFhirUrl(Integer userId, String iisFhirUrl);

    Boolean existsByIdAndUserId(String id, Integer userId);

    Optional<ImmunizationRegistry> findByIdAndUserId(String id, Integer userId);

    Boolean existsByNameAndUserId(String name, Integer userId);

    void deleteByIdAndUserId(String id, Integer userId);
}