package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.SubscriptionStore;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SubscriptionStoreRepository extends CrudRepository<SubscriptionStore, String> {
    Optional<SubscriptionStore> findByIdentifier(String id);

}