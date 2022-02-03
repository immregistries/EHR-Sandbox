package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpSession;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.immregistries.ehr.model.ImmunizationRegistry;
import org.immregistries.ehr.model.Tester;



public abstract class ResourceClient {
    private static final FhirContext CTX = CustomClientBuilder.getCTX();
    private static final String TENANT_B = "default";

    private ResourceClient(){}


    public static String read(String resourceType, String resourceId, HttpSession session){
        ImmunizationRegistry ir = (ImmunizationRegistry) session.getAttribute("IR");
        return read(resourceType, resourceId, ir.getIisFHIRUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static String write(IBaseResource resource, HttpSession session){
        ImmunizationRegistry ir = (ImmunizationRegistry) session.getAttribute("IR");
        return write(resource, ir.getIisFHIRUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static String delete(String resourceType, String resourceId, HttpSession session){
        ImmunizationRegistry ir = (ImmunizationRegistry) session.getAttribute("IR");
        return delete(resourceType, resourceId, ir.getIisFHIRUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static String update(IBaseResource resource, String resourceId, HttpSession session){
        ImmunizationRegistry ir = (ImmunizationRegistry) session.getAttribute("IR");
        return update(resource, resourceId, ir.getIisFHIRUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }
  
  
    public static String read(String resourceType, String iisUrl, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
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
  
  
    public static String write(IBaseResource resource, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        MethodOutcome outcome;
        String response;
        try {
           // Create the resource on the server
           outcome = client.create().resource(resource).execute();
           // Log the ID that the server assigned
           IIdType id = outcome.getId();
           if (id != null){
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
                LocalDateTime now = LocalDateTime.now();  
                response = dtf.format(now) + " Created resource, got ID: " + id.getIdPart();
           }else {
               response = "Response includes no id";
           }
        } catch (DataFormatException e) {
           response = "ERROR Writing FHIR Resource";
           e.printStackTrace();
        }
        return response;
    }

  
    public static String delete(String resourceType, String iisUrl, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
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

    public static String update(IBaseResource resource, String iisUrl, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        String response;
        MethodOutcome outcome = client.update().resource(resource).execute();

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
