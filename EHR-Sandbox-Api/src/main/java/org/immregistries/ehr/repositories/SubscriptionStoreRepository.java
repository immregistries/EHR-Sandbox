package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.SubscriptionStore;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SubscriptionStoreRepository extends CrudRepository<SubscriptionStore, String> {
    Optional<SubscriptionStore> findByIdentifier(String id);
//    Optional<SubscriptionStore> findByIdentifier(int id);
}