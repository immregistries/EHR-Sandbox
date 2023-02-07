package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PatientRepository extends CrudRepository<EhrPatient, String> {

    Iterable<EhrPatient> findByTenantIdAndFacilityId(Integer tenantId, Integer facilityId);

    Optional<EhrPatient> findByTenantIdAndFacilityIdAndId(Integer tenantId, Integer facilityId, String id);

    Optional<EhrPatient> findByFacilityIdAndId(Integer facilityId, String id);
    Boolean existsByFacilityIdAndId(Integer facilityId, String id);


    Iterable<EhrPatient> findByTenantId(Integer tenantId);
}