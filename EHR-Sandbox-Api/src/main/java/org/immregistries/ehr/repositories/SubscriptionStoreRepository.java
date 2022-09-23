package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.SubscriptionStore;
import org.immregistries.ehr.entities.Tenant;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SubscriptionStoreRepository extends CrudRepository<SubscriptionStore, String> {
    Optional<SubscriptionStore> findByIdentifier(String id);

}