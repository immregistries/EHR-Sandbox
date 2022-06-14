package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.SubscriptionStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionStoreRepository extends JpaRepository<SubscriptionStore, String> {
}