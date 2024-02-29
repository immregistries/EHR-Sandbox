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

    Optional<EhrPatient> findByFacilityIdAndMrn(String facilityId, String mrn);

    @Query(value = "SELECT p FROM EhrPatient p RIGHT JOIN Facility f on p.facility = f  WHERE f.tenant = :tenant")
    Iterable<EhrPatient> findByTenantId(@Param("tenant") Tenant tenant);

//    Iterable<EhrPatient> findByTenantIdAndFacilityId(String tenantId, String facilityId);
//    Optional<EhrPatient> findByTenantIdAndFacilityIdAndId(String tenantId, String facilityId, String id);

    Iterable<EhrPatient> findByFacilityId(String facilityId);
//    Set<EhrPatient> findAllByFacilityIdAndId(String facilityId, Iterable<String> id);
    Optional<EhrPatient> findByFacilityIdAndId(String facilityId, String id);
    Boolean existsByFacilityIdAndId(String facilityId, String id);



}