package org.immregistries.ehr.logic;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.immregistries.ehr.api.entities.EhrEntity;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.http.ResponseEntity;

import java.util.Set;

/**
 * Interface for Services parsing and importing external bundles in local systsm
 */
public interface IBundleImportService {
    ResponseEntity<String> importBundle(ImmunizationRegistry immunizationRegistry, Facility facility, IBaseBundle iBaseBundle);

    Set<EhrEntity> viewBundleAndMatchIdentifiers(ImmunizationRegistry immunizationRegistry, Facility facility, IBaseBundle iBaseBundle, Boolean includeOnlyGolden);
}
