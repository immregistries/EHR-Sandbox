package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Patient;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PatientRepository extends CrudRepository<Patient, Integer> {

    Iterable<Patient> findByTenantIdAndFacilityId(Integer tenantId, Integer facilityId);

    Optional<Patient> findByTenantIdAndFacilityIdAndId(Integer tenantId, Integer facilityId, Integer id);

    Optional<Patient> findByFacilityIdAndId(Integer facilityId, Integer id);
    Boolean existsByFacilityIdAndId(Integer facilityId, Integer id);


    Iterable<Patient> findByTenantId(Integer tenantId);
}