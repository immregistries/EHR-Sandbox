package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.ResourceType;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;

public interface EhrFhirProvider<FhirResourceType extends IBaseResource> {
    Class<FhirResourceType> getResourceType();
    ResourceType getResourceName();
    MethodOutcome update(FhirResourceType resourceType, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);
//    MethodOutcome delete(IdType theId, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);
    MethodOutcome deleteConditional(IdType theId, String theConditionalUrl, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);


}
