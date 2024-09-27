package org.immregistries.ehr.logic;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.entities.EhrEntity;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

/**
 * Interface for Services parsing and importing external bundles in local systsm
 */
public interface IBundleImportService {
    ResponseEntity<String> importBundle(ImmunizationRegistry immunizationRegistry, Facility facility, IBaseBundle iBaseBundle);

    Set<EhrEntity> viewBundleAndMatchIdentifiers(ImmunizationRegistry immunizationRegistry, Facility facility, IBaseBundle iBaseBundle, Boolean includeOnlyGolden);

    List<IBaseResource> baseResourcesFromBaseBundleEntries(String resource);

    List<IDomainResource> domainResourcesFromBaseBundleEntries(String resource);

    List<IBaseResource> baseResourcesFromBaseBundleEntries(IBaseBundle iBaseBundle);

    List<IDomainResource> domainResourcesFromBaseBundleEntries(IBaseBundle iBaseBundle);

}
