package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Feedback;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {
    Optional<Feedback> findByPatientId(String patientId);

    void deleteByVaccinationEventIdAndSeverity(String vaccinationEventId, String severity);

    void deleteByVaccinationEventIdAndIisAndSeverity(String vaccinationEventId, String iis, String severity);

    void deleteByPatientIdAndSeverityAndVaccinationEventNull(String ehrPatientId, String severity);

    void deleteByPatientIdAndIisAndSeverityAndVaccinationEventNull(String ehrPatientId, String iis, String severity);

}