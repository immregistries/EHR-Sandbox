package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.SubscriptionInfo;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionInfoRepository extends CrudRepository<SubscriptionInfo, String> {
}