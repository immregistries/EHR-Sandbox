package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@Conditional(OnR5Condition.class)
public class ResourceClientR5 extends ResourceClient<Identifier>{

    public MethodOutcome delete(String resourceType, String resourceId, IGenericClient client) {
        MethodOutcome outcome = client.delete().resourceById(new IdType(resourceType, resourceId)).execute();
        OperationOutcome opeOutcome = (OperationOutcome) outcome.getOperationOutcome();
        return outcome;
    }

    public MethodOutcome update(IBaseResource resource, String resourceId, IGenericClient client) {
        MethodOutcome outcome = client.update().resource(resource).execute();
        return outcome;
    }

    public MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, ImmunizationRegistry ir) {
        return  updateOrCreate(resource, type, identifier, customClientFactory.newGenericClient(ir));
    }
    public MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, IGenericClient client) {
        MethodOutcome outcome;
        try {
            if (identifier != null && identifier.getValue() != null && !identifier.getValue().isEmpty()) {
                outcome = client.update().resource(resource).conditionalByUrl(
                        type+"?identifier="+identifier.getSystem()+"|"+identifier.getValue()
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
}
