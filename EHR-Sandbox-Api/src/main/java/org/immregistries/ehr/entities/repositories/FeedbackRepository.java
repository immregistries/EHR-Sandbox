package org.immregistries.ehr.entities.repositories;

import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.Feedback;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {
    Optional<Feedback>  findByPatientId(Integer patientId);

}