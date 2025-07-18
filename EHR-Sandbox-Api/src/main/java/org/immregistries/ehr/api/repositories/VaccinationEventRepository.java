package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.User;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VaccinationEventRepository extends JpaRepository<VaccinationEvent, Integer>, RevisionRepository<VaccinationEvent, Integer, Integer> {

    @Query(value = "SELECT v FROM VaccinationEvent v RIGHT JOIN Facility f on v.administeringFacility = f RIGHT JOIN Tenant t on f.tenant = t  WHERE t.user = :user")
    Iterable<VaccinationEvent> findByUserId(@Param("user") User user);

    @Query(value = "SELECT v FROM VaccinationEvent v RIGHT JOIN Facility f on v.administeringFacility = f RIGHT JOIN Tenant t on f.tenant = t  WHERE t.user = :user AND v.id = :id")
    Optional<VaccinationEvent> findByUserIdAndId(@Param("user") User user, @Param("id") Integer id);

    Optional<VaccinationEvent> findByPatientIdAndId(Integer patientId, Integer id);

    Optional<VaccinationEvent> findByAdministeringFacilityIdAndId(Integer facilityId, Integer id);

    List<VaccinationEvent> findByAdministeringFacilityId(Integer facilityId);

    Boolean existsByAdministeringFacilityIdAndId(Integer facilityId, Integer id);

    Boolean existsByPatientIdAndId(Integer patientId, Integer id);

    List<VaccinationEvent> findByPatientId(Integer patientId);

    List<VaccinationEvent> findOneByPatientId(Integer patientId);

}