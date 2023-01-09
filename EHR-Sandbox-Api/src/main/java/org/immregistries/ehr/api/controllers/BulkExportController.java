package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.StringType;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.logic.CustomClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class BulkExportController {
    @Autowired
    private ImmRegistryController immRegistryController;
    @Autowired
    CustomClientBuilder customClientBuilder;

    @GetMapping("/iim-registry/{immRegistryId}/Group/{groupId}/$export")
    public ResponseEntity<String> bulkKickOff(@PathVariable() Integer immRegistryId, @PathVariable()  String groupId, @RequestParam Optional<String> _type) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        IGenericClient client = customClientBuilder.newGenericClient(ir);
//        String response = client.operation().onServer().named("$export")
//                .withNoParameters(Parameters.class)
//                .withAdditionalHeader("").useHttpGet().execute().toString()
//        return ResponseEntity.ok(response);
        // Adding new interceptor to add "Prefer Header"
//        AdditionalRequestHeadersInterceptor additionalRequestHeadersInterceptor = new AdditionalRequestHeadersInterceptor();
//        additionalRequestHeadersInterceptor.addHeaderValue("Prefer","respond-async");
//        client.registerInterceptor(additionalRequestHeadersInterceptor);
        // In order to get the response headers
        CapturingInterceptor capturingInterceptor = new CapturingInterceptor();
        client.registerInterceptor(capturingInterceptor);


        Parameters inParams = new Parameters();
        _type.ifPresent(s -> inParams.addParameter().setName("_type").setValue(new StringType(s)));


        Parameters outParams =
//        String respMessage =
                client.operation()
                .onInstance(new IdType("Group", groupId))
                .named("$export")
                .withParameters(inParams)
                .useHttpGet()
                .withAdditionalHeader("Prefer", "respond-async")
                .execute();
//                .toString();

        IHttpResponse response = capturingInterceptor.getLastResponse();
        if (response.getStatus() != 202) {
            throw new RuntimeException(response.getStatusInfo());
        }
        String contentLocationUrl = response.getHeaders("Content-Location").get(0);
//        return ResponseEntity.ok(contentLocationUrl);
        return ResponseEntity.ok(contentLocationUrl);
    }

    @GetMapping("/iim-registry/{immRegistryId}/$export-status")
    public ResponseEntity bulkStatus(@PathVariable() Integer immRegistryId, @RequestParam String contentUrl) {
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
            con.setRequestProperty("Authorization", "Basic " + encoded);
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
//        Parameters outParams = client.operation().onServer()
//                .named("$export")
//                .withParameters(inParams)
//                .useHttpGet()
//                .withAdditionalHeader("Prefer","respond-async")
//                .execute();
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
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            String encoded = Base64.getEncoder()
                    .encodeToString((ir.getIisUsername() + ":" + ir.getIisPassword())
                            .getBytes(StandardCharsets.UTF_8));  //Java 8
            con.setRequestProperty("Authorization", "Basic " + encoded);
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
//        Parameters outParams = client.operation().onServer()
//                .named("$export")
//                .withParameters(inParams)
//                .useHttpGet()
//                .withAdditionalHeader("Prefer","respond-async")
//                .execute();
    }

    @GetMapping("/iim-registry/{immRegistryId}/$export-result")
    public ResponseEntity bulkResult(@PathVariable() Integer immRegistryId, @RequestParam String contentUrl) {
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
            String encoded = Base64.getEncoder()
                    .encodeToString((ir.getIisUsername() + ":" + ir.getIisPassword())
                            .getBytes(StandardCharsets.UTF_8));  //Java 8
            con.setRequestProperty("Authorization", "Basic " + encoded);
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
//                wait();
//                return ResponseEntity.ok(con.getHeaderFields());
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
}
