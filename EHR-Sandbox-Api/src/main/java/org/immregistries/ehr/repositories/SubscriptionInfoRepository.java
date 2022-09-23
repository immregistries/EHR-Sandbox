package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.SubscriptionInfo;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionInfoRepository extends CrudRepository<SubscriptionInfo, String> {
}