package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.api.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static org.immregistries.ehr.logic.mapping.interfaces.IPatientMapper.MRN_TYPE_VALUE;

public interface EhrPatientRepository extends CrudRepository<EhrPatient, Integer>, RevisionRepository<EhrPatient, Integer, Integer> {

    @Query(value = "SELECT p FROM EhrPatient p INNER JOIN p.identifiers i WHERE p.facility.id = :facilityId AND i.type = '" + MRN_TYPE_VALUE + "' ANd i.value = :mrn")
    Optional<EhrPatient> findByFacilityIdAndMrn(@Param("facilityId") Integer facilityId, @Param("mrn") String mrn);

    @Query(value = "SELECT p FROM EhrPatient p INNER JOIN p.identifiers i WHERE p.facility.id = :facilityId AND i.system = ':system' ANd i.value = :value")
    Optional<EhrPatient> findOneByFacilityIdAndIdentifier(@Param("facilityId") Integer facilityId, @Param("system") String system, @Param("value") String value);

    @Query(value = "SELECT DISTINCT p FROM EhrPatient p INNER JOIN p.identifiers i WHERE p.facility.id = :facilityId AND i.system = ':system' ANd i.value = :value")
    Iterable<EhrPatient> findByFacilityIdAndIdentifier(@Param("facilityId") Integer facilityId, @Param("system") String system, @Param("value") String value);

    @Query(value = "SELECT DISTINCT p FROM EhrPatient p INNER JOIN p.identifiers i WHERE p.facility.id = :facilityId ANd i.value = :value")
    Iterable<EhrPatient> findByFacilityIdAndIdentifierValue(@Param("facilityId") Integer facilityId, @Param("value") String value);

    @Query(value = "SELECT DISTINCT p FROM EhrPatient p INNER JOIN p.identifiers i WHERE p.facility.id = :facilityId ANd i.system = :system")
    Iterable<EhrPatient> findByFacilityIdAndIdentifierSystem(@Param("facilityId") Integer facilityId, @Param("system") String system);

    @Query(value = "SELECT p FROM EhrPatient p RIGHT JOIN Facility f on p.facility.id = f.id  WHERE f.tenant = :tenant")
    Iterable<EhrPatient> findByTenantId(@Param("tenant") Tenant tenant);

    @Query(value = "SELECT p FROM EhrPatient p RIGHT JOIN Facility f on p.facility.id = f.id RIGHT JOIN Tenant t on f.tenant = t  WHERE t.user = :user")
    Iterable<EhrPatient> findByUserId(@Param("user") User user);

    @Query(value = "SELECT p FROM EhrPatient p RIGHT JOIN Facility f on p.facility = f RIGHT JOIN Tenant t on f.tenant = t  WHERE t.user = :user AND p.id = :id")
    Optional<EhrPatient> findByUserIdAndId(@Param("user") User user, @Param("id") Integer id);

//    Iterable<EhrPatient> findByTenantIdAndFacilityId(Integer tenantId, Integer facilityId);
//    Optional<EhrPatient> findByTenantIdAndFacilityIdAndId(Integer tenantId, Integer facilityId, String id);

    Iterable<EhrPatient> findByFacilityId(Integer facilityId);

    //    Set<EhrPatient> findAllByFacilityIdAndId(Integer facilityId, Iterable<Integer> id);
    Optional<EhrPatient> findByFacilityIdAndId(Integer facilityId, Integer id);

    Boolean existsByFacilityIdAndId(Integer facilityId, Integer id);


}