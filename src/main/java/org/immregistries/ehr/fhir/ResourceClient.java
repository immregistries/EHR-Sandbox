package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import javax.servlet.http.HttpSession;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.immregistries.ehr.model.Tester;



public abstract class ResourceClient {
    private static final FhirContext CTX = CustomClientBuilder.getCTX();
    private static final String TENANT_B = "default";

    private ResourceClient(){}

    public static String read(String resourceType, String resourceId){
        return read(resourceType, resourceId, TENANT_B, TENANT_B, TENANT_B);
    }

    public static String write(IBaseResource resource){
        return write(resource, TENANT_B, TENANT_B, TENANT_B);
    }

    public static String delete(String resourceType, String resourceId){
        return delete(resourceType, resourceId, TENANT_B, TENANT_B, TENANT_B);
    }

    public static String read(String resourceType, String resourceId, HttpSession session){
        Tester tester = (Tester) session.getAttribute("tester");
        return read(resourceType, resourceId, tester.getLoginUsername(), tester.getLoginUsername(), tester.getLoginPassword());
    }

    public static String write(IBaseResource resource, HttpSession session){
        Tester tester = (Tester) session.getAttribute("tester");
        return write(resource, tester.getLoginUsername(), tester.getLoginUsername(), tester.getLoginPassword());
    }

    public static String delete(String resourceType, String resourceId, HttpSession session){
        Tester tester = (Tester) session.getAttribute("tester");
        return delete(resourceType, resourceId, tester.getLoginUsername(), tester.getLoginUsername(), tester.getLoginPassword());
    }
  
  
    public static String read(String resourceType, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(tenantId, username, password).getClient();
        IBaseResource resource;
        String response;
        try {
            resource = client.read().resource(resourceType).withId(resourceId).execute();
            // resource = client.read().resource(Patient.class).withId(resourceId).execute();
            response = CTX.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
          } catch (ResourceNotFoundException e) {
            response = "Resource not found";
            e.printStackTrace();
          }
        return response;
    }
  
  
    public static String write(IBaseResource resource,  String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(tenantId, username, password).getClient();
        MethodOutcome outcome;
        String response;
        try {
           // Create the resource on the server
           outcome = client.create().resource(resource).execute();
           // Log the ID that the server assigned
           IIdType id = outcome.getId();
           if (id != null){
                response = "Created resource, got ID: " + id.getIdPart();
           }else {
               response = "Response includes no id";
           }
        } catch (DataFormatException e) {
           response = "ERROR Writing FHIR Resource";
           e.printStackTrace();
        }
        return response;
    }

  
    public static String delete(String resourceType, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(tenantId, username, password).getClient();
        String response;
        MethodOutcome outcome = client.delete().resourceById(new IdType(resourceType, resourceId)).execute();

        OperationOutcome opeOutcome = (OperationOutcome) outcome.getOperationOutcome();
        if (opeOutcome != null) {
            response = opeOutcome.getIssueFirstRep().getDetails().getCodingFirstRep().getCode();
        }
        else{
            response = "Resource not found";
        }
        return response;
    }
    
}
