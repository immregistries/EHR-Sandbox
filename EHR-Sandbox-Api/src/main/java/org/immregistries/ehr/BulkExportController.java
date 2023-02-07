package org.immregistries.ehr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.DateType;
import org.immregistries.ehr.api.controllers.ImmRegistryController;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationIdentifier;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.PatientIdentifier;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.ImmunizationIdentifierRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.repositories.PatientIdentifierRepository;
import org.immregistries.ehr.fhir.ServerR5.ImmunizationProviderR5;
import org.immregistries.ehr.fhir.ServerR5.PatientProviderR5;
import org.immregistries.ehr.fhir.Client.CustomClientBuilder;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class BulkExportController {
    private static final Logger logger = LoggerFactory.getLogger(BulkExportController.class);
    @Autowired
    FhirContext fhirContext;
    @Autowired
    ImmRegistryController immRegistryController;
    @Autowired
    CustomClientBuilder customClientBuilder;
    @Autowired
    FacilityRepository facilityRepository;
    @Autowired
    ApplicationContext context;
    @Autowired
    PatientProviderR5 patientProvider;
    @Autowired
    ImmunizationProviderR5 immunizationProvider;


    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;


    @Autowired
    private ImmunizationIdentifierRepository immunizationIdentifierRepository;
    @Autowired
    private PatientIdentifierRepository patientIdentifierRepository;

    @Autowired
    private ResourceIdentificationService resourceIdentificationService;


    @GetMapping("/iim-registry/{immRegistryId}/Group/{groupId}/$export-synch")
    public ResponseEntity<byte[]> bulkKickOffSynch(@PathVariable() Integer immRegistryId, @PathVariable()  String groupId
            ,@RequestParam Optional<String> _outputFormat
            ,@RequestParam Optional<String> _type
            ,@RequestParam Optional<Date> _since
            ,@RequestParam Optional<String> _typeFilter
            ,@RequestParam Optional<String> _elements
            ,@RequestParam Optional<String> includeAssociatedData
            ,@RequestParam Optional<String> patient
            ,@RequestParam Optional<Boolean> _mdm
    ) throws IOException {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        IGenericClient client = customClientBuilder.newGenericClient(ir);
        // In order to get the response headers
        CapturingInterceptor capturingInterceptor = new CapturingInterceptor();
        client.registerInterceptor(capturingInterceptor);

        Parameters inParams = new Parameters();
        _outputFormat.ifPresent(s -> inParams.addParameter().setName("_outputFormat").setValue(new StringType(s)));
        _type.ifPresent(s -> inParams.addParameter().setName("_type").setValue(new StringType(s)));
        _since.ifPresent(d -> inParams.addParameter().setName("_since").setValue(new DateType(d)));
        _typeFilter.ifPresent(s -> inParams.addParameter().setName("_typeFilter").setValue(new StringType(s)));
        _elements.ifPresent(s -> inParams.addParameter().setName("_elements").setValue(new StringType(s)));
        patient.ifPresent(s -> inParams.addParameter().setName("patient").setValue(new StringType(s)));
        includeAssociatedData.ifPresent(s -> inParams.addParameter().setName("includeAssociatedData").setValue(new StringType(s)));
        _mdm.ifPresent(b -> inParams.addParameter().setName("_mdm").setValue(new BooleanType(b)));

        Parameters outParams = client.operation()
                        .onInstance(new IdType("Group", groupId))
                        .named("$export")
                        .withParameters(inParams).accept("*/*")
                        .useHttpGet()
                        .withAdditionalHeader("Prefer", "respond-sync")
                        .execute();
        IHttpResponse response = capturingInterceptor.getLastResponse();
        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response.readEntity().readAllBytes());
        } else {
            return ResponseEntity.badRequest().body(response.getStatusInfo().getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/iim-registry/{immRegistryId}/Group/{groupId}/$export-asynch")
    public ResponseEntity<String> bulkKickOffAsynch(@PathVariable() Integer immRegistryId, @PathVariable()  String groupId
            ,@RequestParam Optional<String> _outputFormat
            ,@RequestParam Optional<String> _type
            ,@RequestParam Optional<Date> _since
            ,@RequestParam Optional<String> _typeFilter
            ,@RequestParam Optional<Boolean> _mdm
    ) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        IGenericClient client = customClientBuilder.newGenericClient(ir);
        // In order to get the response headers
        CapturingInterceptor capturingInterceptor = new CapturingInterceptor();
        client.registerInterceptor(capturingInterceptor);

        Parameters inParams = new Parameters();
        _outputFormat.ifPresent(s -> inParams.addParameter().setName("_outputFormat").setValue(new StringType(s)));
        _type.ifPresent(s -> inParams.addParameter().setName("_type").setValue(new StringType(s)));
        _since.ifPresent(d -> inParams.addParameter().setName("_since").setValue(new DateType(d)));
        _typeFilter.ifPresent(s -> inParams.addParameter().setName("_typeFilter").setValue(new StringType(s)));
        _mdm.ifPresent(b -> inParams.addParameter().setName("_mdm").setValue(new BooleanType(b)));

        Parameters outParams =
                client.operation()
                .onInstance(new IdType("Group", groupId))
                .named("$export")
                .withParameters(inParams)
                .useHttpGet()
                .withAdditionalHeader("Prefer", "respond-async")
                .execute();
        IHttpResponse response = capturingInterceptor.getLastResponse();
        if (response.getStatus() == 202) {
            String contentLocationUrl = response.getHeaders("Content-Location").get(0);
            return ResponseEntity.ok(contentLocationUrl);
        }
        return ResponseEntity.internalServerError().body(response.getStatusInfo());
    }

    @GetMapping("/iim-registry/{immRegistryId}/$export-status")
    public ResponseEntity bulkCheckStatus(@PathVariable() Integer immRegistryId, @RequestParam String contentUrl) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        Map<String, List<String>> result;
        // URL used is the one gotten from the kickoff, while authentication remains the same
//        IGenericClient client = customClientBuilder.newGenericClient(contentLocationUrl,ir.getIisPassword(),ir.getIisUsername());
//        client.operation().onInstance(new IdType("Group",));
        HttpURLConnection con = null;
        URL url;
        try {
            url = new URL(contentUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            String encoded = Base64.getEncoder()
                    .encodeToString((ir.getIisUsername() + ":" + ir.getIisPassword())
                            .getBytes(StandardCharsets.UTF_8));  //Java 8
            if (!contentUrl.contains("x-amz-security-token") && !ir.getIisPassword().isBlank()) {
                con.setRequestProperty("Authorization", customClientBuilder.authorisationTokenContent(ir));
            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
                return ResponseEntity.ok(con.getInputStream().readAllBytes());
            } else if (status == 202) {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<String,List<String>> entry: con.getHeaderFields().entrySet()) {
                    builder.append(entry.getKey()).append("=");
                    for (String header: entry.getValue()) {
                        builder.append(header).append(",\t");
                    }
                    builder.append("\n");
                }
                return ResponseEntity.ok(builder.toString());
            } else {
                return ResponseEntity.internalServerError().body(con.getResponseMessage());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    @DeleteMapping("/iim-registry/{immRegistryId}/$export-status")
    public ResponseEntity bulkDelete(@PathVariable() Integer immRegistryId, @RequestParam String contentUrl) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        HttpURLConnection con = null;
        URL url;
        try {
            url = new URL(contentUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Accept", "application/json");

            if (!contentUrl.contains("x-amz-security-token") && !ir.getIisPassword().isBlank()) {
                con.setRequestProperty("Authorization", customClientBuilder.authorisationTokenContent(ir));
            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 202) {
                return ResponseEntity.ok(con.getInputStream().readAllBytes());
            } else {
                return ResponseEntity.internalServerError().body(con.getResponseMessage());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    @GetMapping("/iim-registry/{immRegistryId}/$export-result")
    public ResponseEntity bulkResult(@PathVariable() Integer immRegistryId, @RequestParam String contentUrl, Optional<Integer> loadInFacility) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        Map<String, List<String>> result;
        // URL used obtain form the content check
        HttpURLConnection con = null;
        URL url;
        try {
            url = new URL(contentUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "*/*");
            if (!contentUrl.contains("x-amz-security-token") && !ir.getIisPassword().isBlank()) {
                con.setRequestProperty("Authorization", customClientBuilder.authorisationTokenContent(ir));
            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            logger.info("RESPONSE {}", status);
            if (status == 200 || status == 202) {
                if (loadInFacility.isPresent()) {
                    Facility facility = facilityRepository.findById(loadInFacility.get()).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified"));
                    return loadNdJson(ir, facility,con.getInputStream());
                }
                return ResponseEntity.ok(con.getInputStream().readAllBytes());
            } else {
                return ResponseEntity.internalServerError().body(con.getResponseMessage());
            }
        } catch (MalformedURLException | ProtocolException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    @PostMapping("/iim-registry/{immRegistryId}/facility/{facilityId}/$load")
    public ResponseEntity bulkResultLoad(@PathVariable() Integer immRegistryId, @RequestBody String ndjson, @PathVariable Integer facilityId) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified"));
        return loadNdJson(ir, facility,ndjson);
    }

    private ResponseEntity<String> loadNdJson(ImmunizationRegistry immunizationRegistry, Facility facility, InputStream ndJsonStream) {
        IParser parser = fhirContext.newNDJsonParser();
        Bundle bundle = (Bundle) parser.parseResource(ndJsonStream);
        return  loadNdJson(immunizationRegistry,facility, bundle);
    }
    private ResponseEntity<String> loadNdJson(ImmunizationRegistry immunizationRegistry, Facility facility, String ndJson) {
        IParser parser = fhirContext.newNDJsonParser();
        Bundle bundle = (Bundle) parser.parseResource(ndJson);
        return  loadNdJson(immunizationRegistry,facility, bundle);
    }
    private ResponseEntity<String> loadNdJson(ImmunizationRegistry immunizationRegistry, Facility facility, Bundle bundle ) {
        StringBuilder responseBuilder = new StringBuilder();
        int count = 0;
        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            switch (entry.getResource().getResourceType()) {
                case Patient: {
                    Patient patient = (Patient) entry.getResource();
                    String receivedId = new IdType(patient.getId()).getIdPart();
                    String dbId; // = patientIdentifier.get(immunizationRegistry.getId()).getOrDefault(receivedId,-1);
                    MethodOutcome methodOutcome = patientProvider.createPatient(patient,facility);
                    dbId = methodOutcome.getId().getValue();
                    patientIdentifierRepository.save(new PatientIdentifier(dbId,immunizationRegistry.getId(),receivedId));
                    responseBuilder.append("\nPatient ").append(receivedId).append(" loaded as patient ").append(dbId);
                    logger.info("Patient  {}  loaded as patient  {}",receivedId,dbId);
                    count++;
                    break;
                }
                case Immunization: {
                    Immunization immunization = (Immunization) entry.getResource();
                    String dbId; // = immunizationIdentifier.get(immunizationRegistry.getId()).getOrDefault(immunization.getId(),-1);
                    String receivedId = new IdType(immunization.getId()).getIdPart();
                    String receivedPatientId = new IdType(immunization.getPatient().getReference()).getIdPart();
                    Optional<PatientIdentifier> patientIdentifier = patientIdentifierRepository.findByPatientIdAndImmunizationRegistryId(receivedPatientId,immunizationRegistry.getId());
                    if (patientIdentifier.isPresent()) {
                        immunization.setPatient(new Reference("Patient/" + patientIdentifier.get().getPatientId()));
                        MethodOutcome methodOutcome = immunizationProvider.createImmunization(immunization,facility);
                        dbId = methodOutcome.getId().getValue();
                        immunizationIdentifierRepository.save(new ImmunizationIdentifier(dbId,immunizationRegistry.getId(),receivedId));
                        responseBuilder.append("\nImmunization ").append(receivedId).append(" loaded as Immunization ").append(dbId);
                        logger.info("Immunization {} loaded as Immunization {}",receivedId,dbId);
                        count++;
                    } else {
                        responseBuilder.append("\nERROR : ").append(immunization.getPatient().getReference()).append(" Unknown");
                        logger.info("ERROR : Patient  {}  Unknown",immunization.getPatient().getReference());
                    }
                    break;
                }
            }
        }
        responseBuilder.append("\nNumber of successful load in facility ").append(facility.getNameDisplay()).append(": ").append(count);
        return ResponseEntity.ok(responseBuilder.toString());
    }

    private String validateNdJsonBundle(Bundle bundle ) {
//        IValidator validator = new
        FhirValidator validator = fhirContext.newValidator();

//        IValidatorModule coreModule = new
        IValidatorModule module = new FhirInstanceValidator(fhirContext);
        validator.registerValidatorModule(module);

        return "";
    }


}
