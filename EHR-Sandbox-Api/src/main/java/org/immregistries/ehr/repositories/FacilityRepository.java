package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.Facility;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FacilityRepository extends CrudRepository<Facility, Integer>, JpaSpecificationExecutor<Facility> {

    Iterable<Facility> findByTenantId(Integer tenantId);
    Optional<Facility> findByIdAndTenantId(Integer id,Integer tenantId);
    Boolean existsByTenantIdAndNameDisplay(Integer tenantId, String nameDisplay);
    Boolean existsByTenantIdAndId(Integer tenantId, Integer id);
}