package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.AuditRevisionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AuditRevisionEntityRepository extends CrudRepository<AuditRevisionEntity, Integer> {
    Iterable<AuditRevisionEntity> findByUserAndTimestampGreaterThan(Integer user, long timestamp);
    boolean existsByUserAndTimestampGreaterThanAndSubscriptionIdNotNull(Integer user, long timestamp);
    boolean existsByUserAndTimestampGreaterThan(Integer user, long timestamp);
}