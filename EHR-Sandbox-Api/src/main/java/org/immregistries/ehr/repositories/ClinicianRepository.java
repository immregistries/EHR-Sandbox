package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.Clinician;
import org.springframework.data.repository.CrudRepository;

public interface ClinicianRepository extends CrudRepository<Clinician, Integer> {
}