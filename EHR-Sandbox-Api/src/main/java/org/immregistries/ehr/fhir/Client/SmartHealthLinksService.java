package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jsonwebtoken.CompressionException;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.logic.shlink.ShCardClaims;
import org.immregistries.ehr.logic.shlink.ShLinkFilePayload;
import org.immregistries.ehr.logic.shlink.ShLinkManifest;
import org.immregistries.ehr.logic.shlink.ShLinkPayload;
import org.jetbrains.annotations.NotNull;
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


    public List<String> importSmartHealthLink(String shlink, String password, PublicKey publicKey) throws JsonProcessingException {
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
            byte[] data = directFileRequestReading(url, recipient);
            Jwe<byte[]> jwe = Jwts.parser().decryptWith(secretKey).build().parseEncryptedContent(new String(data));
            logger.info("df payload {}\n base64 {}\n", new String(jwe.getPayload()));
            result.addAll(processShCardJwe(jwe, publicKey));
        } else { // Manifest
            ShLinkManifest shLinkManifest = manifestReading(url, recipient, password, SmartHealthCardService.MAXIMUM_DATA_SIZE);
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

    private List<String> embeddedFile(ShLinkManifest.FileManifest manifestFile, SecretKey secretKey, PublicKey publicKey) throws JsonProcessingException {
        Gson gson = new Gson();
        switch (manifestFile.getContentType()) {
            case "application/smart-health-card": {
                Jwe<byte[]> jwe = Jwts.parser().decryptWith(secretKey).build().parseEncryptedContent(manifestFile.getEmbedded());
                return processShCardJwe(jwe, publicKey);
            }
            case "application/fhir+json": { //TODO test
                Jwe<byte[]> jwe = Jwts.parser().decryptWith(secretKey).build().parseEncryptedContent(manifestFile.getEmbedded());
                return List.of(gson.toJson(new String(jwe.getPayload())));
            }
            case "application/smart-api-access": //TODO
            default: {
                throw new RuntimeException("Manifest Content type not supported");
            }
        }
    }

    @NotNull
    private List<String> processShCardJwe(Jwe<byte[]> jwe, PublicKey publicKey) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = new String(jwe.getPayload());
        ShLinkFilePayload shLinkFilePayload = objectMapper.readValue(payload, ShLinkFilePayload.class);
        List<String> verifiableCredentials = shLinkFilePayload.getVerifiableCredential();
        List<String> resultList = new ArrayList<>();
        for (String compact : verifiableCredentials) {
            ShCardClaims.VerifiableCredential verifiableCredential = null;
            try {
                /**
                 * Verify Signature
                 */
                verifiableCredential = smartHealthCardService.parseVCFromCompactJwt(publicKey, compact);
            } catch (CompressionException compressionException) {
                // Do unverified raw inflate if compression headers are invalid
//                        compressionException.printStackTrace();
                verifiableCredential = smartHealthCardService.parseVCFromCompactJwtUnsecure(compact);
            }
            String s = objectMapper.writeValueAsString(verifiableCredential);
            resultList.add(s);
        }
        return resultList;
    }

    /**
     * Executes  the direct file request
     *
     * @param baseUrl
     * @param recipient
     * @return Operation Result
     */
    public byte[] directFileRequestReading(String baseUrl, String recipient) {
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
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (MalformedURLException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the manifest reading
     *
     * @param url
     * @param recipient
     * @param passcode
     * @param embeddedLengthMax
     * @return the manifest
     */
    public ShLinkManifest manifestReading(String url, String recipient, String passcode, Integer embeddedLengthMax) {
        URI uri = null;
        ObjectMapper objectMapper = new ObjectMapper();
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
//            logger.info("manifest reading response code: {} result: {} headers: {}", response.statusCode(), response.body(), response.headers());
            if (StringUtils.isBlank(response.body())) {
                throw new RuntimeException("Error retrieving Manifest: status code " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), ShLinkManifest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid Manifest: " + e.getMessage());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException("Error when retrieving Manifest", e);
        }
    }


}
