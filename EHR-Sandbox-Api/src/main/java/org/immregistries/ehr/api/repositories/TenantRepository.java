package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.Tenant;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TenantRepository extends CrudRepository<Tenant, Integer> {

    Optional<Tenant> findById(Integer id);

    Iterable<Tenant> findByUserId(int id);

    Optional<Tenant> findByIdAndUserId(Integer id, int userId);

    Boolean existsByIdAndUserId(Integer id, int userId);

    Boolean existsByUserIdAndNameDisplay(int userId, String nameDisplay);

    Boolean existsByUserId(int id);

}