package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

public interface IResourceClient {
//    String search(String resourceType, ImmunizationRegistry ir);
//
//    String search(String resourceType, IGenericClient client);

    String read(String resourceType, String resourceId, ImmunizationRegistry ir);

    String read(String resourceType, String resourceId, IGenericClient client);

    MethodOutcome create(IBaseResource resource, ImmunizationRegistry ir);

    MethodOutcome create(IBaseResource resource, IGenericClient client);

    MethodOutcome update(IBaseResource resource, String resourceId, ImmunizationRegistry ir);

    MethodOutcome delete(String resourceType, String resourceId, ImmunizationRegistry ir);


    MethodOutcome delete(String resourceType, String resourceId, IGenericClient client);

    MethodOutcome update(IBaseResource resource, String resourceId, IGenericClient client);

    MethodOutcome updateOrCreate(IBaseResource resource, String type, EhrIdentifier identifier, ImmunizationRegistry ir);

    public MethodOutcome updateOrCreate(IBaseResource resource, String type, EhrIdentifier identifier, IGenericClient client);


}
