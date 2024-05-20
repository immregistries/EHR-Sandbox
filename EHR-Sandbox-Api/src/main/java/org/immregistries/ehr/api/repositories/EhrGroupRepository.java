package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Tenant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EhrGroupRepository extends CrudRepository<EhrGroup, String> {

    @Query(value = "SELECT g FROM EhrGroup g RIGHT JOIN Facility f on g.facility = f  WHERE f.tenant.id = :tenantId")
    Iterable<EhrGroup> findByTenantId(@Param("tenantId") String tenantId);

    @Query(value = "SELECT g FROM EhrGroup g RIGHT JOIN Facility f on g.facility = f  WHERE f.tenant.id = :tenantId AND g.id = :id")
    Optional<EhrGroup> findByTenantIdAndId(@Param("tenantId") String tenantId, @Param("id") String id);

    @Query(value = "SELECT g FROM EhrGroup g RIGHT JOIN Facility f on g.facility = f  WHERE f.tenant = :tenant")
    Iterable<EhrGroup> findByTenant(@Param("tenant") Tenant tenant);

    Iterable<EhrGroup> findByFacilityIdAndImmunizationRegistryId(String facilityId, Integer immunizationRegistryId);

    Iterable<EhrGroup> findByFacilityId(String facilityId);

    Optional<EhrGroup> findByFacilityIdAndImmunizationRegistryIdAndName(String facilityId, Integer immunizationRegistryId, String name);

    Optional<EhrGroup> findByFacilityIdAndId(String facilityId, String id);

    Optional<EhrGroup> findByFacilityIdAndName(String facilityId, String name);

    boolean existsByFacilityIdAndName(String facilityId, String name);

}