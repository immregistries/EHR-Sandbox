package org.immregistries.ehr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.DateType;
import org.immregistries.ehr.api.controllers.ImmunizationRegistryController;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.logic.BundleImportService;
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

@RestController
public class BulkExportController {

    private static final Logger logger = LoggerFactory.getLogger(BulkExportController.class);
    @Autowired
    FhirContext fhirContext;
    @Autowired
    ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    CustomClientFactory customClientFactory;
    @Autowired
    FacilityRepository facilityRepository;

    @Autowired
    BundleImportService bundleImportService;


    @GetMapping("/registry/{registryId}/Group/{groupId}/$export-synch")
    public ResponseEntity<byte[]> bulkKickOffSynch(@PathVariable() Integer registryId, @PathVariable()  String groupId
            ,@RequestParam Optional<String> _outputFormat
            ,@RequestParam Optional<String> _type
            ,@RequestParam Optional<Date> _since
            ,@RequestParam Optional<String> _typeFilter
            ,@RequestParam Optional<String> _elements
            ,@RequestParam Optional<String> includeAssociatedData
            ,@RequestParam Optional<String> patient
            ,@RequestParam Optional<Boolean> _mdm
    ) throws IOException {
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
        IGenericClient client = customClientFactory.newGenericClient(ir);
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

    @GetMapping("/registry/{registryId}/Group/{groupId}/$export-asynch")
    public ResponseEntity<String> bulkKickOffAsynch(@PathVariable() Integer registryId, @PathVariable()  String groupId
            ,@RequestParam Optional<String> _outputFormat
            ,@RequestParam Optional<String> _type
            ,@RequestParam Optional<Date> _since
            ,@RequestParam Optional<String> _typeFilter
            ,@RequestParam Optional<Boolean> _mdm
    ) {
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
        IGenericClient client = customClientFactory.newGenericClient(ir);
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

    @GetMapping("/registry/{registryId}/$export-status")
    public ResponseEntity bulkCheckStatus(@PathVariable() Integer registryId, @RequestParam String contentUrl) {
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
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
                con.setRequestProperty("Authorization", customClientFactory.authorisationTokenContent(ir));
            } else {
                con.setRequestProperty("Authorization", "Basic " + encoded);

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

    @DeleteMapping("/registry/{registryId}/$export-status")
    public ResponseEntity bulkDelete(@PathVariable() Integer registryId, @RequestParam String contentUrl) {
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
        HttpURLConnection con = null;
        URL url;
        try {
            url = new URL(contentUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Accept", "application/json");

            if (!contentUrl.contains("x-amz-security-token") && !ir.getIisPassword().isBlank()) {
                con.setRequestProperty("Authorization", customClientFactory.authorisationTokenContent(ir));
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

    @GetMapping("/registry/{registryId}/$export-result")
    public ResponseEntity bulkResult(@PathVariable() Integer registryId, @RequestParam String contentUrl, Optional<Integer> loadInFacility) {
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
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
                con.setRequestProperty("Authorization", customClientFactory.authorisationTokenContent(ir));
            }
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            logger.info("RESPONSE {}", status);
            if (status == 200 || status == 202) {
                if (loadInFacility.isPresent()) {
                    Facility facility = facilityRepository.findById(loadInFacility.get()).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified"));
                    IParser parser = fhirContext.newNDJsonParser();
                    Bundle bundle = (Bundle) parser.parseResource(con.getInputStream());
                    return bundleImportService.importBundle(ir,facility, bundle);
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

}
