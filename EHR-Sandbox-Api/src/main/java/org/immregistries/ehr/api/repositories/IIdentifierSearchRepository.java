package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.EhrEntityWithIdentifiers;
import org.springframework.data.repository.query.Param;

public interface IIdentifierSearchRepository<T extends EhrEntityWithIdentifiers> {

    Iterable<T> findByFacilityIdAndIdentifier(@Param("facilityId") Integer facilityId, @Param("system") String system, @Param("value") String value);

    Iterable<T> findByFacilityIdAndIdentifierValue(@Param("facilityId") Integer facilityId, @Param("value") String value);

    Iterable<T> findByFacilityIdAndIdentifierSystem(@Param("facilityId") Integer facilityId, @Param("system") String system);

    Iterable<T> findByFacilityId(@Param("facilityId") Integer facilityId);


}
