package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrSubscription;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EhrSubscriptionRepository extends CrudRepository<EhrSubscription, String> {
    Optional<EhrSubscription> findByIdentifier(String id);
    Optional<EhrSubscription> findByExternalId(String externalId);

}