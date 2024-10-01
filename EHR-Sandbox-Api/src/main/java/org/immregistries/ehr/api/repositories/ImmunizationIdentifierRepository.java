package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.ImmunizationIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.ImmunizationIdentifierKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ImmunizationIdentifierRepository extends CrudRepository<ImmunizationIdentifier, ImmunizationIdentifierKey> {
    Optional<ImmunizationIdentifier> findByVaccinationEventIdAndImmunizationRegistryId(Integer vaccinationEventId, Integer immunizationRegistryId);

    Optional<ImmunizationIdentifier> findByIdentifierAndImmunizationRegistryId(String identifier, Integer immunizationRegistryId);

}