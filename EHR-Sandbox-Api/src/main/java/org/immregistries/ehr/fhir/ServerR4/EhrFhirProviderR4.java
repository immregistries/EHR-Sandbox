package org.immregistries.ehr.fhir.ServerR4;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ResourceType;
import org.immregistries.ehr.fhir.EhrFhirProvider;

public interface EhrFhirProviderR4<FhirResourceType extends IBaseResource> extends EhrFhirProvider<FhirResourceType> {
    ResourceType getResourceName();

}
