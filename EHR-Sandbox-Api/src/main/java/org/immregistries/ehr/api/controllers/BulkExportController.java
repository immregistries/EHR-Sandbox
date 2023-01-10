package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.DateType;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.ServerR4.ImmunizationProviderR4;
import org.immregistries.ehr.fhir.ServerR4.PatientProviderR4;
import org.immregistries.ehr.fhir.Client.CustomClientBuilder;
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
    PatientProviderR4 patientProvider;

    @Autowired
    ImmunizationProviderR4 immunizationProvider;

    @GetMapping("/iim-registry/{immRegistryId}/Group/{groupId}/$export-synch")
    public ResponseEntity<String> bulkKickOffSynch(@PathVariable() Integer immRegistryId, @PathVariable()  String groupId
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
        logger.info("_type {}", _type.get());
        _type.ifPresent(s -> inParams.addParameter().setName("_type").setValue(new StringType(s)));
        _since.ifPresent(d -> inParams.addParameter().setName("_since").setValue(new DateType(d)));
        _typeFilter.ifPresent(s -> inParams.addParameter().setName("_typeFilter").setValue(new StringType(s)));
        _mdm.ifPresent(b -> inParams.addParameter().setName("_mdm").setValue(new BooleanType(b)));

        MethodOutcome outParams =
                client.operation()
                        .onInstance(new IdType("Group", groupId))
                        .named("$export")
                        .withParameters(inParams).accept("*/*")
                        .useHttpGet()
                        .withAdditionalHeader("Prefer", "respond-sync")
                        .returnMethodOutcome()
                        .execute();
        IHttpResponse response = capturingInterceptor.getLastResponse();
        if (response.getStatus() != 200) {
            throw new RuntimeException(response.getStatusInfo());
        }
//        String contentLocationUrl = response.getHeaders("Content-Location").get(0);
//        response.get
        IParser jsonParser = fhirContext.newJsonParser();
        return ResponseEntity.ok(jsonParser.encodeResourceToString(outParams.getResource()));
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
        if (response.getStatus() != 202) {
            throw new RuntimeException(response.getStatusInfo());
        }
        String contentLocationUrl = response.getHeaders("Content-Location").get(0);
        return ResponseEntity.ok(contentLocationUrl);
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
            con.setRequestProperty("Authorization", customClientBuilder.authorisationTokenContent(ir));
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
//                wait();
//                return ResponseEntity.ok(con.getHeaderFields());
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
                return ResponseEntity.ok("ERROR");
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
            con.setRequestProperty("Authorization", customClientBuilder.authorisationTokenContent(ir));
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 202) {
                return ResponseEntity.ok(con.getInputStream().readAllBytes());
            } else {
                return ResponseEntity.ok("ERROR");
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
//        IGenericClient client = customClientBuilder.newGenericClient(contentLocationUrl,ir.getIisPassword(),ir.getIisUsername());
//        client.operation().onInstance(new IdType("Group",));
        HttpURLConnection con = null;
        URL url;
        try {
            url = new URL(contentUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/fhir+ndjson");
            con.setRequestProperty("Authorization", customClientBuilder.authorisationTokenContent(ir));
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
                if (loadInFacility.isPresent()) {
                    Facility facility = facilityRepository.findById(loadInFacility.get()).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified"));
                    return loadNdJson(ir, facility,con.getInputStream());
                }
                return ResponseEntity.ok(con.getInputStream().readAllBytes());
            } else {
                return ResponseEntity.ok("ERROR");
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

    private ResponseEntity<String> loadNdJson(ImmunizationRegistry immunizationRegistry, Facility facility, InputStream ndJsonStream) {
        Map<Integer,Map<String,Integer>> immunizationIdentifier = (Map<Integer, Map<String, Integer>>) context.getBean("ImmunizationIdentifier");
        Map<Integer,Map<String,Integer>> patientIdentifier = (Map<Integer, Map<String, Integer>>) context.getBean("PatientIdentifier");
        immunizationIdentifier.putIfAbsent(immunizationRegistry.getId(),new HashMap<>(100));
        patientIdentifier.putIfAbsent(immunizationRegistry.getId(),new HashMap<>(100));
        IParser parser = fhirContext.newNDJsonParser();
        Bundle bundle = (Bundle) parser.parseResource(ndJsonStream);
        StringBuilder responseBuilder = new StringBuilder();
        int count = 0;
        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            switch (entry.getResource().getResourceType()) {
                case Patient: {
                    Patient patient = (Patient) entry.getResource();
                    String receivedId = patient.getId().split("Patient/")[1].split("/")[0];
                    Integer dbId; // = patientIdentifier.get(immunizationRegistry.getId()).getOrDefault(receivedId,-1);
                    MethodOutcome methodOutcome = patientProvider.createPatient(patient,facility);
                    dbId = Integer.parseInt(methodOutcome.getId().getValue());
                    patientIdentifier.get(immunizationRegistry.getId()).putIfAbsent(receivedId, dbId);
                    responseBuilder.append("\nPatient ").append(receivedId).append(" loaded as patient ").append(dbId);
                    count++;
                    break;
                }
                case Immunization: {
                    Immunization immunization = (Immunization) entry.getResource();
                    Integer dbId; // = immunizationIdentifier.get(immunizationRegistry.getId()).getOrDefault(immunization.getId(),-1);
                    String receivedId = immunization.getId().split("Immunization/")[1].split("/")[0];
                    String receivedPatientId = immunization.getPatient().getReference().split("Patient/")[1].split("/")[0];
                    Integer dbPatientId = patientIdentifier.get(immunizationRegistry.getId()).getOrDefault(receivedPatientId,-1);
                    if (dbPatientId == -1) {
                        responseBuilder.append("\nERROR : ").append(immunization.getPatient().getReference()).append(" Unknown");
                    } else {
                        immunization.setPatient(new Reference("Patient/" + dbPatientId));
                        MethodOutcome methodOutcome = immunizationProvider.createImmunization(immunization,facility);
                        dbId = Integer.parseInt(methodOutcome.getId().getValue());
                        immunizationIdentifier.get(immunizationRegistry.getId()).putIfAbsent(receivedId, dbId);
                        responseBuilder.append("\nImmunization ").append(receivedId).append(" loaded as Immunization ").append(dbId);
                        count++;
                    }
                    break;
                }
            }
        }
        responseBuilder.append("\nNumber of successful load in facility ").append(facility.getNameDisplay()).append(": ").append(count);
        return ResponseEntity.ok(responseBuilder.toString());
    }


}
