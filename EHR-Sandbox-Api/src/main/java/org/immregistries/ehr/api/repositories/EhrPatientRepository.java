package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;

public interface EhrPatientRepository extends CrudRepository<EhrPatient, String>, RevisionRepository<EhrPatient, String ,Integer> {

    Iterable<EhrPatient> findByTenantIdAndFacilityId(Integer tenantId, Integer facilityId);

    Optional<EhrPatient> findByTenantIdAndFacilityIdAndId(Integer tenantId, Integer facilityId, String id);

    Optional<EhrPatient> findByFacilityIdAndId(Integer facilityId, String id);
    Boolean existsByFacilityIdAndId(Integer facilityId, String id);


    Iterable<EhrPatient> findByTenantId(Integer tenantId);
}