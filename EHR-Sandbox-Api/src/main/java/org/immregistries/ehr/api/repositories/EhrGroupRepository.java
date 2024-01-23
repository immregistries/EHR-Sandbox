package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EhrGroupRepository extends CrudRepository<EhrGroup, Integer> {
    Optional<EhrGroup> findByFacilityIdAndId(Integer facilityId, Integer id);
    Optional<EhrGroup> findByFacilityIdAndName(Integer facilityId, String name);
    boolean existsByFacilityIdAndName(Integer facilityId, String name);

}