package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Tenant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static org.immregistries.ehr.logic.mapping.IPatientMapper.MRN_TYPE_VALUE;

public interface EhrPatientRepository extends CrudRepository<EhrPatient, Integer>, RevisionRepository<EhrPatient, Integer, Integer> {

    @Query(value = "SELECT p FROM EhrPatient p INNER JOIN p.identifiers i WHERE p.facility.id = :facilityId AND i.type = '" + MRN_TYPE_VALUE + "' ANd i.value = :mrn")
    Optional<EhrPatient> findByFacilityIdAndMrn(@Param("facilityId") Integer facilityId, @Param("mrn") String mrn);

    @Query(value = "SELECT p FROM EhrPatient p INNER JOIN p.identifiers i WHERE p.facility.id = :facilityId AND i.system = ':system' ANd i.value = :value")
    Optional<EhrPatient> findByFacilityIdAndIdentifier(@Param("facilityId") Integer facilityId, @Param("system") String system, @Param("value") String value);

    @Query(value = "SELECT p FROM EhrPatient p RIGHT JOIN Facility f on p.facility = f  WHERE f.tenant = :tenant")
    Iterable<EhrPatient> findByTenantId(@Param("tenant") Tenant tenant);

//    Iterable<EhrPatient> findByTenantIdAndFacilityId(Integer tenantId, Integer facilityId);
//    Optional<EhrPatient> findByTenantIdAndFacilityIdAndId(Integer tenantId, Integer facilityId, String id);

    Iterable<EhrPatient> findByFacilityId(Integer facilityId);

    //    Set<EhrPatient> findAllByFacilityIdAndId(Integer facilityId, Iterable<Integer> id);
    Optional<EhrPatient> findByFacilityIdAndId(Integer facilityId, Integer id);

    Boolean existsByFacilityIdAndId(Integer facilityId, Integer id);


}