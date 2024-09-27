package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.*;
import io.jsonwebtoken.CompressionException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class SmartHealthLinksService {
    private static final String ISSUER_URL = "https://spec.smarthealth.cards/examples/issuer";
    private static final Logger logger = LoggerFactory.getLogger(SmartHealthLinksService.class);
    public static final String EMBEDDED = "embedded";
    public static final String CONTENT_TYPE = "contentType";
    public static final String FILES = "files";
    public static final String U = "U";
    public static final String URL = "url";
    public static final String KEY = "key";
    public static final String FLAG = "flag";
    public static final String EXP = "exp";
    public static final String LABEL = "label";
    public static final String V = "v";
    public static final String SHLINK_PREFIX = "shlink:/";
    public static final String LOCATION = "location";
    public static final String VERIFIABLE_CREDENTIAL = "verifiableCredential";
    public static final String APPLICATION_JOSE = "application/jose";

    @Autowired
    FacilityRepository facilityRepository;
    @Autowired
    SmartHealthCardService smartHealthCardService;
    //    @Autowired
//    EhrFhirClientFactory ehrFhirClientFactory;
    //    @Autowired
//    EhrFhirClientFactory ehrFhirClientFactory;
//    @Autowired
//    EhrFhirClientFactory ehrFhirClientFactory;
    @Autowired()
    @Qualifier("fhirContextR5")
    FhirContext fhirContextR5;
    @Autowired()
    @Qualifier("fhirContextR4")
    FhirContext fhirContextR4;


    public List<String> importSmartHealthLink(String shlink, String password, PublicKey publicKey) {
        Gson gson = new Gson();
        List<String> result = new ArrayList<>(3);
        if (!shlink.startsWith(SHLINK_PREFIX)) {
            throw new RuntimeException("Not prefixed with shlink");
        }
        String decodedFrom64 = new String(Base64.getDecoder().decode(shlink.substring(SHLINK_PREFIX.length()).getBytes()));
        JsonObject payloadObject = JsonParser.parseString(decodedFrom64).getAsJsonObject();
        logger.info("shlink {}", payloadObject);

        String url = payloadObject.get(URL).getAsString();
        String key = payloadObject.get(KEY).getAsString();
        String flags = null;
        JsonElement flagsObject = payloadObject.get(FLAG);
        if (flagsObject != null) {
            flags = flagsObject.getAsString();
        }
        String exp = null;
        JsonElement expObject = payloadObject.get(EXP);
        if (expObject != null) {
            exp = expObject.getAsString();
        }
        String label = null;
        JsonElement labelObject = payloadObject.get(LABEL);
        if (labelObject != null) {
            label = labelObject.getAsString();
        }
        String v = null;
        JsonElement vObject = payloadObject.get(V);
        if (vObject != null) {
            v = vObject.getAsString();
        }

        String recipient = "EHR-sandbox-test";
        byte[] encodedKey = Base64.getUrlDecoder().decode(key.getBytes());
        SecretKey secretKey = new SecretKeySpec(encodedKey, "AES");
        /**
         * if flag contains "U"
         */
        if (StringUtils.isNotBlank(flags) && flags.toUpperCase().contains(U)) {
            String data = directFileRequest(url, recipient);
            Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(data);
            result.add(gson.toJson(jwt.getPayload()));
        } else { // Manifest
            String manifest = manifestReading(url, recipient, password, SmartHealthCardService.MAXIMUM_DATA_SIZE);
            logger.info("manifest {}", manifest);
            JsonObject manifestElement = (JsonObject) JsonParser.parseString(manifest);
            JsonArray files = manifestElement.get(FILES).getAsJsonArray();
            for (JsonElement file : files) {
                JsonObject manifestFile = (JsonObject) file;
                if (manifestFile.has(EMBEDDED)) {
                    result.addAll(embeddedFile(manifestFile, secretKey, publicKey));
                } else if (manifestFile.has(LOCATION)) {//TODO
                    HttpRequest request = HttpRequest.newBuilder()
                            .header(CONTENT_TYPE, APPLICATION_JOSE)
                            .uri(URI.create(manifestFile.get(LOCATION).getAsString()))
                            .build();
                } else {
                    throw new RuntimeException("Manifest File Requires either Embedded or Location");
                }
            }
        }
        return result;
    }

    private List<String> embeddedFile(JsonObject manifestFile, SecretKey secretKey, PublicKey publicKey) {
        Gson gson = new Gson();
        switch (manifestFile.get(CONTENT_TYPE).getAsString()) {
            case "application/smart-health-card": {
                Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(manifestFile.get(EMBEDDED).getAsString());
                JsonArray verifiableCredentials = gson.toJsonTree(jwt.getPayload()).getAsJsonObject().getAsJsonArray(VERIFIABLE_CREDENTIAL);
                List<String> result = new ArrayList<>(verifiableCredentials.size());
                for (JsonElement compact : verifiableCredentials) {
                    String res;
                    try {
                        /**
                         * Verify Signature
                         */
                        res = smartHealthCardService.parseVCFromCompactJwt(publicKey, compact.getAsString());
                    } catch (CompressionException compressionException) {
                        // Do unverified raw inflate if compression headers are invalid
//                        compressionException.printStackTrace();
                        res = smartHealthCardService.parseVCFromCompactJwtUnsecure(compact.getAsString());
                    }
                    result.add(res);
                }
                return result;
            }
            case "application/fhir+json": { //TODO test
                Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(manifestFile.get(EMBEDDED).getAsString());
                return List.of(gson.toJson(jwt.getPayload()));
            }
            case "application/smart-api-access": //TODO
            default: {
                throw new RuntimeException("Manifest Content type not supported");
            }
        }
    }

    public String directFileRequest(String baseUrl, String recipient) {
        try {
            if (baseUrl.contains("?")) {
                baseUrl += "&";
            } else {
                baseUrl += "?";
            }
            baseUrl += "recipient=" + recipient;
            URI uri = URI.create(baseUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (MalformedURLException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String manifestReading(String url, String recipient, String passcode, Integer embeddedLengthMax) {
        URI uri = null;
        try {
            uri = new URI(url);
            Gson gson = new Gson();
            JsonObject bodyObject = new JsonObject();
            bodyObject.addProperty("recipient", recipient);
            if (StringUtils.isNotBlank(passcode)) {
                bodyObject.addProperty("passcode", passcode);
            }
            if (embeddedLengthMax != null) {
                bodyObject.addProperty("embeddedLengthMax", embeddedLengthMax);
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(bodyObject)))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
