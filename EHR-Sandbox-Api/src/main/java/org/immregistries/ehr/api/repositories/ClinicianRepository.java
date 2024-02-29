package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Clinician;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClinicianRepository extends CrudRepository<Clinician, Integer> {
    Iterable<Clinician> findByTenantId(String tenantId);
    Optional<Clinician> findByTenantIdAndId(String tenantId, Integer clinicianId);
    boolean existsByTenantIdAndId(String tenantId, Integer clinicianId);
}