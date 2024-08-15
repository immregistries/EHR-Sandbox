package org.immregistries.ehr.fhir.Server;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;

public interface EhrFhirProvider<FhirResourceType extends IBaseResource> {
    Class<FhirResourceType> getResourceType();

    MethodOutcome update(FhirResourceType resourceType, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);
//    MethodOutcome delete(IdType theId, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);
//    MethodOutcome deleteConditional(IdType theId, String theConditionalUrl, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);


}
