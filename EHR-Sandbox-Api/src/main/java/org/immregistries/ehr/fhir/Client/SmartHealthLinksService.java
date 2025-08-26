package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import io.jsonwebtoken.CompressionException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.logic.shlink.ShLinkManifest;
import org.immregistries.ehr.logic.shlink.ShLinkPayload;
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
    private static final Logger logger = LoggerFactory.getLogger(SmartHealthLinksService.class);
    public static final String U_FLAG = "U";
    public static final String SHLINK_PREFIX = "shlink:/";
    public static final String VERIFIABLE_CREDENTIAL = "verifiableCredential";
    public static final String APPLICATION_JOSE = "application/jose";

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
        ObjectMapper mapper = new ObjectMapper();

        List<String> result = new ArrayList<>(3);
        if (!shlink.startsWith(SHLINK_PREFIX)) {
            throw new RuntimeException("Not prefixed with shlink");
        }
        String decodedFrom64 = new String(Base64.getUrlDecoder().decode(shlink.substring(SHLINK_PREFIX.length()).getBytes()));
        ShLinkPayload shLinkPayload;
        try {
            shLinkPayload = mapper.readValue(decodedFrom64, ShLinkPayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid smart health link Payload");
        }
        logger.info("shlink {}", shLinkPayload);

        String url = shLinkPayload.getUrl();
        String key = shLinkPayload.getKey().orElse("");
        String flags = shLinkPayload.getFlag().orElse("");

        String recipient = "EHR-sandbox-test";
        SecretKey secretKey = null;
        if (StringUtils.isNotBlank(key)) {
            byte[] encodedKey = Base64.getUrlDecoder().decode(key.getBytes());
            secretKey = new SecretKeySpec(encodedKey, "AES");
        }
        /**
         * if flag contains "U"
         */
        if (StringUtils.isNotBlank(flags) && flags.toUpperCase().contains(U_FLAG)) {
            String data = directFileRequest(url, recipient);
            Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(data);
            result.add(gson.toJson(jwt.getPayload()));
        } else { // Manifest
            String manifest = manifestReading(url, recipient, password, SmartHealthCardService.MAXIMUM_DATA_SIZE);
//            logger.info("manifest {}", manifest);
            ShLinkManifest shLinkManifest;
            try {
                shLinkManifest = mapper.readValue(manifest, ShLinkManifest.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid Manifest: " + e.getMessage());
            }

            for (ShLinkManifest.FileManifest file : shLinkManifest.getFiles()) {
                if (StringUtils.isNotBlank(file.getEmbedded())) {
                    result.addAll(embeddedFile(file, secretKey, publicKey));
                } else if (StringUtils.isNotBlank(file.getLocation())) {//TODO
                    result.add(file.getLocation());
                } else {
                    throw new RuntimeException("Manifest File Requires either Embedded or Location");
                }
            }
        }
        return result;
    }

    private List<String> embeddedFile(ShLinkManifest.FileManifest manifestFile, SecretKey secretKey, PublicKey publicKey) {
        Gson gson = new Gson();
        switch (manifestFile.getContentType()) {
            case "application/smart-health-card": {
                Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(manifestFile.getEmbedded());
//                else if (publicKey != null) {
//                    jwt = Jwts.parser().verifyWith(publicKey).build().parse(manifestFile.getEmbedded());
//                } else {
//                    /*
//                    Dirty solution for testing by removing the signature of the jwt
//                     */
//                    String embeddedInfo = "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0." + StringUtils.substringBetween(manifestFile.getEmbedded(), ".", ".") + ".";
//                    jwt = Jwts.parser().unsecured().unsecuredDecompression().build().parse(embeddedInfo);
//                }

                String payload = new String((byte[]) jwt.getPayload());

                JsonArray verifiableCredentials = JsonParser.parseString(payload).getAsJsonObject().getAsJsonArray(VERIFIABLE_CREDENTIAL);
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
                Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(manifestFile.getEmbedded());
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
            logger.info("Manifest URI {}", uri);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(bodyObject)))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("manifest reading response code: {} result: {} headers: {}", response.statusCode(), response.body(), response.headers());
            if (StringUtils.isBlank(response.body())) {
                throw new RuntimeException("Error retrieving Manifest: status code " + response.statusCode());
            }
            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
