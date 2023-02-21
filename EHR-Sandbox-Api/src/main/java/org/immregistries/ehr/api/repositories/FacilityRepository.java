package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacilityRepository extends CrudRepository<Facility, Integer>, JpaSpecificationExecutor<Facility> {

    @Query(value = "SELECT f FROM Facility f RIGHT JOIN Tenant t ON f.tenant = t WHERE t.user = :user")
    Iterable<Facility> findByUser(@Param("user") User user);
    @Query(value = "SELECT f FROM Facility f RIGHT JOIN Tenant t ON f.tenant = t WHERE t.user = :user AND f.id = :id")
    Boolean existsByUserAndId(@Param("user") User user, @Param("id") Integer id);
    Iterable<Facility> findByTenantId(Integer tenantId);
    Optional<Facility> findByIdAndTenantId(Integer id,Integer tenantId);
    Boolean existsByTenantIdAndNameDisplay(Integer tenantId, String nameDisplay);
    Boolean existsByTenantIdAndId(Integer tenantId, Integer id);
}