package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;

public interface VaccinationEventRepository extends CrudRepository<VaccinationEvent, Integer>, RevisionRepository<VaccinationEvent, Integer, Integer> {

    Optional<VaccinationEvent> findByPatientIdAndId(Integer patientId, Integer id);

    Optional<VaccinationEvent> findByAdministeringFacilityIdAndId(Integer facilityId, Integer id);

    Iterable<VaccinationEvent> findByAdministeringFacilityId(Integer facilityId);

    Boolean existsByAdministeringFacilityIdAndId(Integer facilityId, Integer id);

    Boolean existsByPatientIdAndId(Integer patientId, Integer id);

    Iterable<VaccinationEvent> findByPatientId(Integer patientId);

    Iterable<VaccinationEvent> findOneByPatientId(Integer patientId);

}