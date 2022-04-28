package org.immregistries.ehr.entities.repositories;

import org.immregistries.ehr.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
//    Boolean existsByEmail(String email);
}