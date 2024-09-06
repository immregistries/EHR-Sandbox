package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class ResourceClient implements IResourceClient {

    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired()
    FhirComponentsDispatcher fhirComponentsDispatcher;


    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public String read(String resourceType, String resourceId, ImmunizationRegistry ir) {
        return read(resourceType, resourceId, fhirComponentsDispatcher.clientFactory().newGenericClient(ir));
    }

    public MethodOutcome create(IBaseResource resource, ImmunizationRegistry ir) {
        return create(resource, fhirComponentsDispatcher.clientFactory().newGenericClient(ir));
    }

    public MethodOutcome delete(String resourceType, String resourceId, ImmunizationRegistry ir) {
        return delete(resourceType, resourceId, fhirComponentsDispatcher.clientFactory().newGenericClient(ir));
    }

    public MethodOutcome update(IBaseResource resource, String resourceId, ImmunizationRegistry ir) {
        return update(resource, resourceId, fhirComponentsDispatcher.clientFactory().newGenericClient(ir));
    }

    public String read(String resourceType, String resourceId, IGenericClient client) {
        IBaseResource resource;
        resource = client.read().resource(resourceType).withId(resourceId).execute();
        return fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    public MethodOutcome create(IBaseResource resource, IGenericClient client) {
        MethodOutcome outcome;
        outcome = client.create().resource(resource).execute();
        return outcome;
    }

    public MethodOutcome update(IBaseResource resource, String resourceId, IGenericClient client) {
        MethodOutcome outcome = client.update().resource(resource).execute();
        return outcome;
    }

    public MethodOutcome updateOrCreate(IBaseResource resource, String type, EhrIdentifier identifier, ImmunizationRegistry ir) {
        return updateOrCreate(resource, type, identifier, fhirComponentsDispatcher.clientFactory().newGenericClient(ir));
    }

    public MethodOutcome updateOrCreate(IBaseResource resource, String type, EhrIdentifier identifier, IGenericClient client) {
        MethodOutcome outcome;
        try {
            if (identifier != null && identifier.getValue() != null && !identifier.getValue().isEmpty()) {
                outcome = client.update().resource(resource).conditionalByUrl(
                        type + "?identifier=" + identifier.getSystem() + "|" + identifier.getValue()
                                + "&_tag:not=http://hapifhir.io/fhir/NamingSystem/mdm-record-status|GOLDEN_RECORD"
                ).execute();
            } else {
                outcome = client.create().resource(resource).execute();
            }

        } catch (ResourceNotFoundException | InvalidRequestException e) {
            outcome = client.create().resource(resource).execute();
        }
        return outcome;
    }

    public MethodOutcome delete(String resourceType, String resourceId, IGenericClient client) {
        MethodOutcome outcome = client.delete().resourceById(resourceType, resourceId).execute();
        return outcome;
    }


}
