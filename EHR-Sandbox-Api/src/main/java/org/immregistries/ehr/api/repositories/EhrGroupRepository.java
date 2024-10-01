package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Tenant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EhrGroupRepository extends CrudRepository<EhrGroup, Integer> {

    @Query(value = "SELECT g FROM EhrGroup g RIGHT JOIN Facility f on g.facility = f  WHERE f.tenant.id = :tenantId")
    Iterable<EhrGroup> findByTenantId(@Param("tenantId") Integer tenantId);

    @Query(value = "SELECT g FROM EhrGroup g RIGHT JOIN Facility f on g.facility = f  WHERE f.tenant.id = :tenantId AND g.id = :id")
    Optional<EhrGroup> findByTenantIdAndId(@Param("tenantId") Integer tenantId, @Param("id") Integer id);

    @Query(value = "SELECT g FROM EhrGroup g RIGHT JOIN Facility f on g.facility = f  WHERE f.tenant = :tenant")
    Iterable<EhrGroup> findByTenant(@Param("tenant") Tenant tenant);

    Iterable<EhrGroup> findByFacilityIdAndImmunizationRegistryId(Integer facilityId, Integer immunizationRegistryId);

    Iterable<EhrGroup> findByFacilityId(Integer facilityId);

    Optional<EhrGroup> findByFacilityIdAndImmunizationRegistryIdAndName(Integer facilityId, Integer immunizationRegistryId, String name);

    Optional<EhrGroup> findByFacilityIdAndId(Integer facilityId, Integer id);

    Optional<EhrGroup> findByFacilityIdAndName(Integer facilityId, String name);

    boolean existsByFacilityIdAndName(Integer facilityId, String name);

}