package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;
import java.util.Optional;

public interface VaccinationEventRepository extends JpaRepository<VaccinationEvent, Integer>, RevisionRepository<VaccinationEvent, Integer, Integer> {

    Optional<VaccinationEvent> findByPatientIdAndId(Integer patientId, Integer id);

    Optional<VaccinationEvent> findByAdministeringFacilityIdAndId(Integer facilityId, Integer id);

    List<VaccinationEvent> findByAdministeringFacilityId(Integer facilityId);

    Boolean existsByAdministeringFacilityIdAndId(Integer facilityId, Integer id);

    Boolean existsByPatientIdAndId(Integer patientId, Integer id);

    List<VaccinationEvent> findByPatientId(Integer patientId);

    List<VaccinationEvent> findOneByPatientId(Integer patientId);

}