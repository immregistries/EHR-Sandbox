package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrSubscriptionInfo;
import org.springframework.data.repository.CrudRepository;

public interface EhrSubscriptionInfoRepository extends CrudRepository<EhrSubscriptionInfo, String> {
}