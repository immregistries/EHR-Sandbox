package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Vaccine;
import org.springframework.data.repository.CrudRepository;

public interface VaccineRepository extends CrudRepository<Vaccine, Integer> {
}