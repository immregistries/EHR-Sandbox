package org.immregistries.ehr.repositories;

import org.immregistries.ehr.entities.Tenant;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TenantRepository extends CrudRepository<Tenant, Integer> {

    Optional<Tenant> findById(int id);
    Iterable<Tenant> findByUserId(int id);
    Optional<Tenant> findByIdAndUserId(int id, int userId);
    Boolean existsByIdAndUserId(int id, int userId);
    Boolean existsByUserIdAndNameDisplay(int userId, String nameDisplay);
    Boolean existsByUserId(int id);

}