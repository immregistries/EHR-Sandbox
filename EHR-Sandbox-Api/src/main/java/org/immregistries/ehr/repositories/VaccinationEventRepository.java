package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VaccinationEventRepository extends CrudRepository<VaccinationEvent, Integer> {

    Optional<VaccinationEvent> findByPatientIdAndId(Integer patientId, Integer id);
    Boolean existsByPatientIdAndId(Integer patientId, Integer id);
    Iterable<VaccinationEvent> findByPatientId(Integer patientId);
    Iterable<VaccinationEvent> findByPatientIdAndAdministeringFacility(Integer patientId, Integer facilityId);

}