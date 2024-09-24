package org.immregistries.ehr.fhir.Client;

import com.google.gson.*;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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

    @Autowired()
    FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    FacilityRepository facilityRepository;
    @Autowired
    SmartHealthCardService smartHealthCardService;


    public List<String> importSmartHealthLink(String shlink, ImmunizationRegistry ir) {
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

        byte[] encryptedPayload = null;
        String decompressed = null;
        String recipient = "EHR-sandbox-test";
        String password = null;
        password = "12345";
        byte[] encodedKey = Base64.getUrlDecoder().decode(key.getBytes());
        SecretKey secretKey = new SecretKeySpec(encodedKey, "AES");
        /**
         * if flag contains "U"
         */
        if (StringUtils.isNotBlank(flags) && flags.toUpperCase().contains(U)) {
            String data = new String(directFileRequest(url, ir, recipient));
            Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(data);
            result.add(gson.toJson(jwt.getPayload()));
        } else {
            String manifest = manifestReading(url, ir, recipient, password, null);
            JsonObject manifestElement = (JsonObject) JsonParser.parseString(manifest);
            JsonArray files = manifestElement.get(FILES).getAsJsonArray();
            for (JsonElement file : files) {
                JsonObject jsonObject = (JsonObject) file;
                switch (jsonObject.get(CONTENT_TYPE).getAsString()) {
                    case "application/smart-health-card":
                    case "application/fhir+json": {
                        break;
                    }
                    case "application/smart-api-access":
                    default: {
                        throw new RuntimeException("Manifest not supported");
                    }
                }
                if (jsonObject.has(EMBEDDED)) {
                    Jwt jwt = Jwts.parser().decryptWith(secretKey).build().parse(jsonObject.get(EMBEDDED).getAsString());
                    logger.info("jwt {}", gson.toJson(jwt));
                    JsonObject jwtPayload = gson.toJsonTree(jwt.getPayload()).getAsJsonObject();
                    JsonArray verifiableCredentials = jwtPayload.getAsJsonArray(VERIFIABLE_CREDENTIAL);
                    String exampleKey = "{" +
                            "\"kty\": \"EC\"," +
                            "\"kid\": \"3Kfdg-XwP-7gXyywtUfUADwBumDOPKMQx-iELL11W9s\"," +
                            "\"use\": \"sig\"," +
                            "\"alg\": \"ES256\"," +
                            "\"crv\": \"P-256\"," +
                            "\"x\": \"11XvRWy1I2S0EyJlyf_bWfw_TQ5CJJNLw78bHXNxcgw\"," +
                            "\"y\": \"eZXwxvO1hvCY0KucrPfKo7yAyMT6Ajc3N7OkAB6VYy8\"," +
                            "\"d\": \"FvOOk6hMixJ2o9zt4PCfan_UW7i4aOEnzj76ZaCI9Og\"" +
                            "}";
                    try {
                        PublicKey publicKey = ECKey.parse(exampleKey).toPublicKey();
                        for (JsonElement compact : verifiableCredentials) {
//                            String res = smartHealthCardService.parseVerifiableCredentialFromCompactJwt(publicKey, compact.getAsString());
                            String res = smartHealthCardService.parseVCFromCompactJwtUnsecure(compact.getAsString());
                            result.add(res);
                        }
                    } catch (ParseException | JOSEException e) {
                        throw new RuntimeException(e);
                    }

                } else if (jsonObject.has(LOCATION)) {

                }
            }
        }
        return result;
    }

    private static String inflate(byte[] data, String key) {
        String decompressed;
        try {
            Inflater inflater = new Inflater();
            if (StringUtils.isNotBlank(key)) {
//                inflater.setDictionary(key.getBytes());
            }
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[SmartHealthCardService.MAXIMUM_DATA_SIZE];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
                logger.info("{}", count);
            }
            outputStream.close();
            byte[] output = outputStream.toByteArray();
            inflater.end();

            decompressed = Base64.getEncoder().encodeToString(output);
            logger.info("result {}",
                    decompressed);
        } catch (DataFormatException | IOException e) {
            throw new RuntimeException(e);
        }
        return decompressed;
    }

    public List<String> importSmartHealthLinkUrl(URL shlinkUrl, ImmunizationRegistry ir) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) shlinkUrl.openConnection();
            con.setRequestMethod("GET");
//            addAuthorizationHeader(con, ir);
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            String shlink = inflate(con.getInputStream().readAllBytes(), null);
//            String shlink = Base64.getEncoder().encodeToString(con.getInputStream().readAllBytes());
            logger.info("url 1 {}", shlink);
            return null;
//            return importSmartHealthLink(shlink, ir);


        } catch (MalformedURLException e) {
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

    private static void addAuthorizationHeader(HttpURLConnection con, ImmunizationRegistry ir) {
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        String encoded = Base64.getEncoder()
                .encodeToString((ir.getIisUsername() + ":" + ir.getIisPassword())
                        .getBytes(StandardCharsets.UTF_8));
        if (!con.getURL().toExternalForm().contains("x-amz-security-token") && StringUtils.isNotBlank(ir.getIisPassword())) {
            con.setRequestProperty("Authorization", EhrFhirClientFactory.authorisationTokenContent(ir));
        } else {
            con.setRequestProperty("Authorization", "Basic " + encoded);
        }
    }

    public byte[] directFileRequest(String baseUrl, ImmunizationRegistry ir, String recipient) {
        URI uri = null;
        HttpURLConnection con = null;
        try {
            if (baseUrl.contains("?")) {
                baseUrl += "&";
            } else {
                baseUrl += "?";
            }
            baseUrl += "recipient=" + recipient;
            uri = new URI(baseUrl);
            URL url = uri.toURL();
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
//            addAuthorizationHeader(con, ir);
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            return con.getInputStream().readAllBytes();
        } catch (MalformedURLException | URISyntaxException e) {
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

    public String manifestReading(String url, ImmunizationRegistry ir, String recipient, String passcode, Integer embeddedLengthMax) {
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
            JsonElement manifest = JsonParser.parseString(response.body());
            logger.info("manifest {} ", manifest);
            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
