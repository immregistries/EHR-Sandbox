package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.PatientExternalIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.PatientExternalIdentifierKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PatientIdentifierRepository extends CrudRepository<PatientExternalIdentifier, PatientExternalIdentifierKey> {
    Optional<PatientExternalIdentifier> findByPatientIdAndImmunizationRegistryId(String patientId, Integer immunizationRegistryId);

    Optional<PatientExternalIdentifier> findByIdentifierAndImmunizationRegistryId(String identifier, Integer immunizationRegistryId);
}