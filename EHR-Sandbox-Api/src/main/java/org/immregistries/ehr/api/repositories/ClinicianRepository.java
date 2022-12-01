package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Clinician;
import org.springframework.data.repository.CrudRepository;

public interface ClinicianRepository extends CrudRepository<Clinician, Integer> {
}