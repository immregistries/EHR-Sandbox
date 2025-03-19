package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Feedback;
import org.springframework.data.repository.CrudRepository;

public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {
    Iterable<Feedback> findByFacilityId(Integer facilityId);

    Iterable<Feedback> findByFacilityIdAndPatientId(Integer facilityId, Integer patientId);

    Iterable<Feedback> findByPatientId(Integer patientId);

    Iterable<Feedback> findByFacilityIdAndVaccinationEventId(Integer facilityId, Integer vaccinationEventId);

    Iterable<Feedback> findByVaccinationEventId(Integer vaccinationEventId);

    void deleteByVaccinationEventIdAndSeverity(Integer vaccinationEventId, String severity);

    void deleteByVaccinationEventIdAndIisAndSeverity(Integer vaccinationEventId, String iis, String severity);

    void deleteByPatientIdAndSeverityAndVaccinationEventNull(Integer ehrPatientId, String severity);

    void deleteByPatientId(Integer ehrPatientId);

    void deleteByPatientIdAndIisAndSeverityAndVaccinationEventNull(Integer ehrPatientId, String iis, String severity);

}