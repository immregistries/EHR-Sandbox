package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.Vaccine;
import org.springframework.data.repository.CrudRepository;

public interface VaccineRepository extends CrudRepository<Vaccine, Integer> {
}