package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Clinician;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClinicianRepository extends CrudRepository<Clinician, Integer> {
    Iterable<Clinician> findByTenantId(Integer tenantId);
    Optional<Clinician> findByTenantIdAndId(Integer tenantId, Integer clinicianId);
    boolean existsByTenantIdAndId(Integer tenantId, Integer clinicianId);
}