package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Feedback;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {
    Optional<Feedback>  findByPatientId(String patientId);
    void deleteByVaccinationEventIdAndSeverity(String vaccinationEventId, String severity);
    void deleteByVaccinationEventIdAndIisAndSeverity(String vaccinationEventId, Integer iis,String severity);
    void deleteByPatientIdAndSeverityAndVaccinationEventNull(String ehrPatientId, String severity);

}