package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;

public interface VaccinationEventRepository extends CrudRepository<VaccinationEvent, String>, RevisionRepository<VaccinationEvent,String,Integer> {

    Optional<VaccinationEvent> findByPatientIdAndId(String patientId, String id);
    Optional<VaccinationEvent> findByAdministeringFacilityIdAndId(Integer facilityId, String id);
    Boolean existsByAdministeringFacilityIdAndId(Integer facilityId, String id);
    Boolean existsByPatientIdAndId(String patientId, String id);
    Iterable<VaccinationEvent> findByPatientId(String patientId);
    Iterable<VaccinationEvent> findOneByPatientId(String patientId);
    Iterable<VaccinationEvent> findByPatientIdAndAdministeringFacility(String patientId, Integer facilityId);

}