package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Vaccine;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;

public interface VaccineRepository extends CrudRepository<Vaccine, Integer>, RevisionRepository<Vaccine, Integer, Long> {
}