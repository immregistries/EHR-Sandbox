package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EhrGroupRepository extends CrudRepository<EhrGroup, Integer> {
    Iterable<EhrGroup> findByFacilityIdAndImmunizationRegistryId(String facilityId, Integer immunizationRegistryId);
    Iterable<EhrGroup> findByFacilityId(String facilityId);
    Optional<EhrGroup> findByFacilityIdAndImmunizationRegistryIdAndName(String facilityId, Integer immunizationRegistryId, String name);
    Optional<EhrGroup> findByFacilityIdAndId(String facilityId, Integer id);
    Optional<EhrGroup> findByFacilityIdAndName(String facilityId, String name);
    boolean existsByFacilityIdAndName(String facilityId, String name);

}