package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;

public interface IResourceClient<Identifier> {
    String read(String resourceType, String resourceId, ImmunizationRegistry ir);
    String read(String resourceType, String resourceId, IGenericClient client);
    MethodOutcome create(IBaseResource resource, ImmunizationRegistry ir);
    MethodOutcome create(IBaseResource resource, IGenericClient client);
    MethodOutcome update(IBaseResource resource, String resourceId, ImmunizationRegistry ir);
    MethodOutcome delete(String resourceType, String resourceId, ImmunizationRegistry ir);


    MethodOutcome delete(String resourceType, String resourceId, IGenericClient client);
    MethodOutcome update(IBaseResource resource, String resourceId, IGenericClient client);
    MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, ImmunizationRegistry ir);
    public MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, IGenericClient client);



    }
