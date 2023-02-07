package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.PatientIdentifier;
import org.immregistries.ehr.api.entities.PatientIdentifierKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PatientIdentifierRepository extends CrudRepository<PatientIdentifier, PatientIdentifierKey> {
    Optional<PatientIdentifier> findByPatientIdAndImmunizationRegistryId(String patientId, Integer immunizationRegistryId);
    Optional<PatientIdentifier> findByIdentifierAndImmunizationRegistryId(String identifier, Integer immunizationRegistryId);
}