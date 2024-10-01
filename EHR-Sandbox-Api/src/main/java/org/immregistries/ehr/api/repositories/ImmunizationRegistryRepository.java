package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ImmunizationRegistryRepository extends CrudRepository<ImmunizationRegistry, Integer> {
    Iterable<ImmunizationRegistry> findByUserId(Integer userId);

    Optional<ImmunizationRegistry> findByUserIdAndIisFhirUrl(Integer userId, String iisFhirUrl);

    Boolean existsByIdAndUserId(Integer id, Integer userId);

    Optional<ImmunizationRegistry> findByIdAndUserId(Integer id, Integer userId);

    Boolean existsByNameAndUserId(String name, Integer userId);

    void deleteByIdAndUserId(Integer id, Integer userId);
}