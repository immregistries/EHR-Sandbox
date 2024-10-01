package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Clinician;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClinicianRepository extends CrudRepository<Clinician, Integer> {
    Iterable<Clinician> findByTenantId(Integer tenantId);

    Optional<Clinician> findByTenantIdAndId(Integer tenantId, Integer clinicianId);

    boolean existsByTenantIdAndId(Integer tenantId, Integer clinicianId);

    @Query(value = "SELECT c FROM Clinician c INNER JOIN c.identifiers i WHERE c.tenant.id = :tenantId AND i.system = ':system' ANd i.value = :value")
    Optional<Clinician> findByTenantIdAndIdentifier(@Param("tenantId") Integer tenantId, @Param("system") String system, @Param("value") String value);
}