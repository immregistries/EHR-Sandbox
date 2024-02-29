package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacilityRepository extends CrudRepository<Facility, String>, JpaSpecificationExecutor<Facility> {

    @Query(value = "SELECT f FROM Facility f RIGHT JOIN Tenant t ON f.tenant = t WHERE t.user = :user")
//    @EntityGraph(value = "Facility.patients")
    Iterable<Facility> findByUser(@Param("user") User user);
    @Query(value = "SELECT f FROM Facility f RIGHT JOIN Tenant t ON f.tenant = t WHERE t.user = :user AND f.id = :id")
//    @EntityGraph(value = "Facility.patients")
    Optional<Facility> findByUserAndId(@Param("user") User user, @Param("id") String id);
    Iterable<Facility> findByTenantId(String tenantId);
    Optional<Facility> findByIdAndTenantId(String id, String tenantId);
    Boolean existsByTenantIdAndNameDisplay(String tenantId, String nameDisplay);
    Boolean existsByTenantIdAndId(String tenantId, String id);
}