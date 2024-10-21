package org.immregistries.ehr;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.EhrEntity;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.Client.EhrFhirClientFactory;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;


@RestController
public class BulkImportController {

    public static final String OUTPUT_FORMAT = "_outputFormat";
    public static final String TYPE = "_type";
    public static final String SINCE = "_since";
    public static final String TYPE_FILTER = "_typeFilter";
    public static final String ELEMENTS = "_elements";
    public static final String PATIENT = "patient";
    public static final String INCLUDE_ASSOCIATED_DATA = "includeAssociatedData";
    public static final String MDM = "_mdm";
    public static final String CONTENT_URL = "contentUrl";
    public static final String LOAD_IN_FACILITY = "loadInFacility";
    private Map<String, String> resultCacheStore;

    private static final Logger logger = LoggerFactory.getLogger(BulkImportController.class);
    @Autowired()
    FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    FacilityRepository facilityRepository;


    @GetMapping(REGISTRY_PATH + "/Group/{groupId}/$export-synch")
    public ResponseEntity<byte[]> bulkKickOffSynch(@PathVariable(REGISTRY_ID) Integer registryId, @PathVariable("groupId") String groupId
            , @RequestParam(OUTPUT_FORMAT) Optional<String> _outputFormat
            , @RequestParam(TYPE) Optional<String> _type
            , @RequestParam(SINCE) Optional<Date> _since
            , @RequestParam(TYPE_FILTER) Optional<String> _typeFilter
            , @RequestParam(ELEMENTS) Optional<String> _elements
            , @RequestParam(INCLUDE_ASSOCIATED_DATA) Optional<String> includeAssociatedData
            , @RequestParam(PATIENT) Optional<String> patient
            , @RequestParam(MDM) Optional<Boolean> _mdm
    ) throws IOException {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
        // In order to get the response headers
        CapturingInterceptor capturingInterceptor = new CapturingInterceptor();
        client.registerInterceptor(capturingInterceptor);

        IBaseParameters inBaseParameters;
        if (ProcessingFlavor.R4.isActive()) {
            org.hl7.fhir.r4.model.Parameters inParams = new org.hl7.fhir.r4.model.Parameters();
            _outputFormat.ifPresent(s -> inParams.addParameter().setName(OUTPUT_FORMAT).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            _type.ifPresent(s -> inParams.addParameter().setName(TYPE).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            _since.ifPresent(d -> inParams.addParameter().setName(SINCE).setValue(new org.hl7.fhir.r4.model.DateType(d)));
            _typeFilter.ifPresent(s -> inParams.addParameter().setName(TYPE_FILTER).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            _elements.ifPresent(s -> inParams.addParameter().setName(ELEMENTS).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            patient.ifPresent(s -> inParams.addParameter().setName(PATIENT).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            includeAssociatedData.ifPresent(s -> inParams.addParameter().setName(INCLUDE_ASSOCIATED_DATA).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            _mdm.ifPresent(b -> inParams.addParameter().setName(MDM).setValue(new org.hl7.fhir.r4.model.BooleanType(b)));
            inBaseParameters = inParams;
        } else {
            org.hl7.fhir.r5.model.Parameters inParams = new org.hl7.fhir.r5.model.Parameters();
            _outputFormat.ifPresent(s -> inParams.addParameter().setName(OUTPUT_FORMAT).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            _type.ifPresent(s -> inParams.addParameter().setName(TYPE).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            _since.ifPresent(d -> inParams.addParameter().setName(SINCE).setValue(new org.hl7.fhir.r5.model.DateType(d)));
            _typeFilter.ifPresent(s -> inParams.addParameter().setName(TYPE_FILTER).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            _elements.ifPresent(s -> inParams.addParameter().setName(ELEMENTS).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            patient.ifPresent(s -> inParams.addParameter().setName(PATIENT).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            includeAssociatedData.ifPresent(s -> inParams.addParameter().setName(INCLUDE_ASSOCIATED_DATA).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            _mdm.ifPresent(b -> inParams.addParameter().setName(MDM).setValue(new org.hl7.fhir.r5.model.BooleanType(b)));
            inBaseParameters = inParams;
        }

        IBaseParameters outParams = client.operation()
                .onInstance("Group/" + groupId)
                .named("$export")
                .withParameters(inBaseParameters).accept("*/*")
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

    @GetMapping(REGISTRY_PATH + "/Group/{groupId}/$export-asynch")
    public ResponseEntity<String> bulkKickOffAsynch(@PathVariable(REGISTRY_ID) Integer registryId, @PathVariable("groupId") String groupId
            , @RequestParam(OUTPUT_FORMAT) Optional<String> _outputFormat
            , @RequestParam(TYPE) Optional<String> _type
            , @RequestParam(SINCE) Optional<Date> _since
            , @RequestParam(TYPE_FILTER) Optional<String> _typeFilter
            , @RequestParam(MDM) Optional<Boolean> _mdm
    ) {
        IHttpResponse response = bulkKickOffHttpResponse(registryId, groupId, _outputFormat, _type, _since, _typeFilter, _mdm);
        if (response.getStatus() == 202) {
            String contentLocationUrl = response.getHeaders("Content-Location").get(0);
            return ResponseEntity.ok(contentLocationUrl);
        }
        return ResponseEntity.internalServerError().body(response.getStatusInfo());
    }

    public IHttpResponse bulkKickOffHttpResponse(@PathVariable(REGISTRY_ID) Integer registryId, @PathVariable("groupId") String groupId
            , @RequestParam(OUTPUT_FORMAT) Optional<String> _outputFormat
            , @RequestParam(TYPE) Optional<String> _type
            , @RequestParam(SINCE) Optional<Date> _since
            , @RequestParam(TYPE_FILTER) Optional<String> _typeFilter
            , @RequestParam(MDM) Optional<Boolean> _mdm) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
        // In order to get the response headers
        CapturingInterceptor capturingInterceptor = new CapturingInterceptor();
        client.registerInterceptor(capturingInterceptor);

        IBaseParameters inBaseParameters;
        if (ProcessingFlavor.R4.isActive()) {
            org.hl7.fhir.r4.model.Parameters inParams = new org.hl7.fhir.r4.model.Parameters();
            _outputFormat.ifPresent(s -> inParams.addParameter().setName(OUTPUT_FORMAT).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            _type.ifPresent(s -> inParams.addParameter().setName(TYPE).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            _since.ifPresent(d -> inParams.addParameter().setName(SINCE).setValue(new org.hl7.fhir.r4.model.DateType(d)));
            _typeFilter.ifPresent(s -> inParams.addParameter().setName(TYPE_FILTER).setValue(new org.hl7.fhir.r4.model.StringType(s)));
            _mdm.ifPresent(b -> inParams.addParameter().setName(MDM).setValue(new org.hl7.fhir.r4.model.BooleanType(b)));
            inBaseParameters = inParams;
        } else {
            org.hl7.fhir.r5.model.Parameters inParams = new org.hl7.fhir.r5.model.Parameters();
            _outputFormat.ifPresent(s -> inParams.addParameter().setName(OUTPUT_FORMAT).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            _type.ifPresent(s -> inParams.addParameter().setName(TYPE).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            _since.ifPresent(d -> inParams.addParameter().setName(SINCE).setValue(new org.hl7.fhir.r5.model.DateType(d)));
            _typeFilter.ifPresent(s -> inParams.addParameter().setName(TYPE_FILTER).setValue(new org.hl7.fhir.r5.model.StringType(s)));
            _mdm.ifPresent(b -> inParams.addParameter().setName(MDM).setValue(new org.hl7.fhir.r5.model.BooleanType(b)));
            inBaseParameters = inParams;
        }

        IBaseParameters outParams = client.operation()
                .onInstance("Group/" + groupId)
                .named("$export")
                .withParameters(inBaseParameters)
                .useHttpGet()
                .withAdditionalHeader("Prefer", "respond-async")
                .execute();
        return capturingInterceptor.getLastResponse();

    }

    @GetMapping(REGISTRY_PATH + "/$export-status")
    public ResponseEntity bulkCheckStatus(@PathVariable(REGISTRY_ID) Integer registryId, @RequestParam(CONTENT_URL) String contentUrl) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
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
                            .getBytes(StandardCharsets.UTF_8));
            if (!contentUrl.contains("x-amz-security-token") && StringUtils.isNotBlank(ir.getIisPassword())) {
                con.setRequestProperty("Authorization", EhrFhirClientFactory.authorisationTokenContent(ir));
            } else {
                con.setRequestProperty("Authorization", "Basic " + encoded);

            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
                return ResponseEntity.ok(con.getInputStream().readAllBytes());
            } else if (status == 202) {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
                    builder.append(entry.getKey()).append("=");
                    for (String header : entry.getValue()) {
                        builder.append(header).append(",\t");
                    }
                    builder.append("\n");
                }
                return ResponseEntity.accepted().body(builder.toString());
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

    public ResponseEntity<?> bulkCheckStatusHttpResponse(ImmunizationRegistry ir, String contentUrl) {
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
                            .getBytes(StandardCharsets.UTF_8));
            if (!contentUrl.contains("x-amz-security-token") && StringUtils.isNotBlank(ir.getIisPassword())) {
                con.setRequestProperty("Authorization", EhrFhirClientFactory.authorisationTokenContent(ir));
            } else {
                con.setRequestProperty("Authorization", "Basic " + encoded);

            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
                return ResponseEntity.ok(con.getInputStream().readAllBytes());
            } else if (status == 202) {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
                    builder.append(entry.getKey()).append("=");
                    for (String header : entry.getValue()) {
                        builder.append(header).append(",\t");
                    }
                    builder.append("\n");
                }
                return ResponseEntity.accepted().body(con.getHeaderFields());
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


    @DeleteMapping(REGISTRY_PATH + "/$export-status")
    public ResponseEntity bulkDelete(@PathVariable(REGISTRY_ID) Integer registryId, @RequestParam(CONTENT_URL) String contentUrl) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        HttpURLConnection con = null;
        URL url;
        try {
            url = new URL(contentUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Accept", "application/json");

            if (!contentUrl.contains("x-amz-security-token") && !ir.getIisPassword().isBlank()) {
                con.setRequestProperty("Authorization", EhrFhirClientFactory.authorisationTokenContent(ir));
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

    @GetMapping(REGISTRY_PATH + "/$export-result")
    public ResponseEntity bulkResult(@PathVariable(REGISTRY_ID) Integer registryId, @RequestParam(CONTENT_URL) String contentUrl, @RequestParam(LOAD_IN_FACILITY) Optional<Integer> loadInFacility) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
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
                con.setRequestProperty("Authorization", EhrFhirClientFactory.authorisationTokenContent(ir));
            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            logger.info("RESPONSE {}", status);
            if (status == 200 || status == 202) {
                if (loadInFacility.isPresent()) {
                    Facility facility = facilityRepository.findById(loadInFacility.get()).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified"));
                    IParser parser = fhirComponentsDispatcher.fhirContext().newNDJsonParser();
                    IBaseBundle bundle = (IBaseBundle) parser.parseResource(con.getInputStream());
                    return fhirComponentsDispatcher.bundleImportService().importBundle(ir, facility, bundle);
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

    //    @GetMapping(PRIMAL_IMM_REGISTRY_SUFFIX + "/$export-result-view")
    public ResponseEntity<Set<EhrEntity>> viewBulkResult(@PathVariable(REGISTRY_ID) Integer registryId, @PathVariable(FACILITY_ID) Integer facilityId, @RequestParam(CONTENT_URL) String contentUrl) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Map<String, List<String>> result;
        // URL used obtain form the content check
        HttpURLConnection con = null;
        URL url;
        Facility facility = facilityRepository.findById(facilityId).get();
        try {
            url = new URL(contentUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "*/*");
            if (!contentUrl.contains("x-amz-security-token") && !ir.getIisPassword().isBlank()) {
                con.setRequestProperty("Authorization", EhrFhirClientFactory.authorisationTokenContent(ir));
            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            logger.info("RESPONSE {}", status);
            if (status == 200 || status == 202) {
                IParser parser = fhirComponentsDispatcher.fhirContext().newNDJsonParser();
                IBaseBundle bundle = (IBaseBundle) parser.parseResource(con.getInputStream());
                return ResponseEntity.ok(fhirComponentsDispatcher.bundleImportService().viewBundleAndMatchIdentifiers(ir, facility, bundle, false));
            }
            return ResponseEntity.internalServerError().build();
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

}
