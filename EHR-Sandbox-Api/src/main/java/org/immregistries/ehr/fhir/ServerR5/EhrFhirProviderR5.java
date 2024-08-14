package org.immregistries.ehr.fhir.ServerR5;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.ResourceType;
import org.immregistries.ehr.fhir.EhrFhirProvider;

public interface EhrFhirProviderR5<FhirResourceType extends IBaseResource> extends EhrFhirProvider<FhirResourceType> {
    ResourceType getResourceName();

}
