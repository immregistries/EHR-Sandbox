package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VaccinationEventRepository extends CrudRepository<VaccinationEvent, Integer> {

    Optional<VaccinationEvent> findByPatientIdAndId(Integer patientId, Integer id);
    Optional<VaccinationEvent> findByAdministeringFacilityIdAndId(Integer facilityId, Integer id);
    Boolean existsByAdministeringFacilityIdAndId(Integer facilityId, Integer id);
    Boolean existsByPatientIdAndId(Integer patientId, Integer id);
    Iterable<VaccinationEvent> findByPatientId(Integer patientId);
    Iterable<VaccinationEvent> findByPatientIdAndAdministeringFacility(Integer patientId, Integer facilityId);

}