package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Tenant;
import org.springframework.data.history.Revisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface EhrPatientRepository extends CrudRepository<EhrPatient, String>, RevisionRepository<EhrPatient, String ,Integer> {

    Optional<EhrPatient> findByFacilityIdAndMrn(Integer facilityId, String mrn);

    @Query(value = "SELECT p FROM EhrPatient p RIGHT JOIN Facility f on p.facility = f  WHERE f.tenant = :tenant")
    Iterable<EhrPatient> findByTenantId(@Param("tenant") Tenant tenant);

//    Iterable<EhrPatient> findByTenantIdAndFacilityId(Integer tenantId, Integer facilityId);
//    Optional<EhrPatient> findByTenantIdAndFacilityIdAndId(Integer tenantId, Integer facilityId, String id);

    Iterable<EhrPatient> findByFacilityId(Integer facilityId);
//    Set<EhrPatient> findAllByFacilityIdAndId(Integer facilityId, Iterable<String> id);
    Optional<EhrPatient> findByFacilityIdAndId(Integer facilityId, String id);
    Boolean existsByFacilityIdAndId(Integer facilityId, String id);



}