package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Feedback;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {
    Optional<Feedback> findByPatientId(Integer patientId);

    void deleteByVaccinationEventIdAndSeverity(Integer vaccinationEventId, String severity);

    void deleteByVaccinationEventIdAndIisAndSeverity(Integer vaccinationEventId, String iis, String severity);

    void deleteByPatientIdAndSeverityAndVaccinationEventNull(Integer ehrPatientId, String severity);

    void deleteByPatientIdAndIisAndSeverityAndVaccinationEventNull(Integer ehrPatientId, String iis, String severity);

}