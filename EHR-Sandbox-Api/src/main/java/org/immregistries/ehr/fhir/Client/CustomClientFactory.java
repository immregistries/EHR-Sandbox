package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.apache.ApacheRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.*;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.logic.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;


/**
 * CustomClientBuilder
 * 
 */
@Component
public class CustomClientFactory extends ApacheRestfulClientFactory implements ITestingUiClientFactory {

    // Needs to be static object and built only one time in whole project
    @Autowired
    FhirContext fhirContext;

    LoggingInterceptor loggingInterceptor;
    private static final Logger logger = LoggerFactory.getLogger(CustomClientFactory.class);
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;


    public IGenericClient newGenericClient(Integer registryId){
         return newGenericClient(immunizationRegistryRepository.findByIdAndUserId(registryId, userDetailsServiceImpl.currentUserId()).orElseThrow(
                 ()-> new RuntimeException("Invalid immunization registry id")
         ));
    }

    public IGenericClient newGenericClient(ImmunizationRegistry registry){
         return newGenericClient(registry.getIisFhirUrl(), registry.getIisFacilityId(), registry.getIisUsername(), registry.getIisPassword(), registry.getHeaders());
    }

    public IGenericClient newGenericClient(String serverURL, String tenantId, String username, String password, String headers) {
        IGenericClient client = newGenericClient(serverURL, tenantId,  username,  password);
        AdditionalRequestHeadersInterceptor additionalRequestHeadersInterceptor = new AdditionalRequestHeadersInterceptor();
        for (String header: headers.split("\n")) {
            String[] headsplit = header.split(":",1);
            if (headsplit.length > 1) {
                additionalRequestHeadersInterceptor.addHeaderValue(headsplit[0], headsplit[1]);
            }
        }
        client.registerInterceptor(additionalRequestHeadersInterceptor);
        return  client;
    }
    public IGenericClient newGenericClient(String serverURL, String tenantId, String username, String password){
        IGenericClient client = newGenericClient(serverURL);
        // Register a tenancy interceptor to add /$tenantid to the url
        UrlTenantSelectionInterceptor tenantSelection = new UrlTenantSelectionInterceptor(tenantId);

        client.registerInterceptor(tenantSelection);
        IClientInterceptor authInterceptor;
        if (username == null || username.isBlank()) {
            /**
             * If username is blank : use token bearer auth
             */
            authInterceptor = new BearerTokenAuthInterceptor(password);
        }else {
            // Create an HTTP basic auth interceptor
            authInterceptor = new BasicAuthInterceptor(username, password);
        }
        client.registerInterceptor(authInterceptor);
        return client;
    }

    public IGenericClient newGenericClient(String serverURL, String username, String password){
        IGenericClient client = newGenericClient(serverURL);
        IClientInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
        client.registerInterceptor(authInterceptor);
        return client;
    }

    @Override
    public synchronized IGenericClient newGenericClient(String theServerBase) {
        asynchInit();
        IGenericClient client = super.newGenericClient(theServerBase);
        client.registerInterceptor(loggingInterceptor);
//        AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
//        interceptor.addHeaderValue("Cache-Control", "no-cache");
//        client.registerInterceptor(interceptor);
        return client;
    }

    private void asynchInit() {
        if (this.getFhirContext() == null ){
            setFhirContext(fhirContext);
            setServerValidationMode(ServerValidationModeEnum.NEVER);
            loggingInterceptor = new LoggingInterceptor();
            loggingInterceptor.setLogger(logger);
            loggingInterceptor.setLogRequestSummary(true);
            loggingInterceptor.setLogRequestBody(true);
        }
    }

    @Override
    public IGenericClient newClient(FhirContext fhirContext, HttpServletRequest httpServletRequest, String s) {
        return null;
    }

    public String authorisationTokenContent(ImmunizationRegistry ir) {
        if (ir.getIisUsername() == null || ir.getIisUsername().isBlank()) {
            /**
             * If username is blank : use token bearer auth
             */
            return "Bearer " + ir.getIisPassword();
        }else {
            // Create an HTTP basic auth interceptor
            String encoded = Base64.getEncoder()
                    .encodeToString((ir.getIisUsername() + ":" + ir.getIisPassword())
                            .getBytes(StandardCharsets.UTF_8));  //Java 8
            return "Basic " + encoded;
        }
    }

    public IGenericClient smartAuthClient(ImmunizationRegistry ir) {
        IGenericClient client = newGenericClient(ir.getIisFhirUrl());
        String authToken = smartGetAuthToken(ir);
        UrlTenantSelectionInterceptor tenantSelection = new UrlTenantSelectionInterceptor(ir.getIisFacilityId());
        client.registerInterceptor(tenantSelection);
        if (authToken != null){
            BearerTokenAuthInterceptor bearerTokenAuthInterceptor = new BearerTokenAuthInterceptor(authToken);
            client.registerInterceptor(bearerTokenAuthInterceptor);
        } else {
            throw new RuntimeException("no token obtained");
        }
        return client;
    }

    public String smartGetAuthToken(ImmunizationRegistry ir) {
        String token_url = smartGetTokenUrl(ir);
        String encodedKey = ir.getIisPassword()
                .replace("-----BEGIN PRIVATE KEY-----","")
                .replace("\n","")
                .replace("-----END PRIVATE KEY-----","");
        byte[] bytes = DatatypeConverter.parseBase64Binary(encodedKey);
        bytes = Base64.getDecoder().decode(encodedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        PrivateKey key;
        try {
            key = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
            logger.info("alg {}", key.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

//         try {
//             KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//             kpg.initialize(4096);
//             KeyPair kp = kpg.generateKeyPair();
//             key = kp.getPrivate();
//         } catch (NoSuchAlgorithmException e) {
//             throw new RuntimeException(e);
//         }
        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(ir.getIisUsername())
                .expiration(new Date(System.currentTimeMillis() + 30000))
                .id("ehr-sandbox-" + RandomStringUtils.random(20, 0, 0, true, true, null, new SecureRandom()))//Generate random id
                .header()
                .type("JWT")
                .keyId(ir.getIisUsername())
                .add("alg","RS384")

                .and()
                .audience().add("test")
                .and()
                .signWith(key);
        String registeringToken = jwtBuilder
                .compact();

        URL url;
        HttpURLConnection con = null;
        try {
            url = new URL(token_url);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            con.setDoOutput(true);
            con.setDoInput(true);
            String body = "{\"client_assertion_type\": \"urn:ietf:params:oauth:client-assertion-type:jwt-bearer\", \"client_assertion\": \"" + registeringToken + "\"}";
            byte[] input = body.getBytes("utf-8");
            con.setRequestProperty( "Content-Length", Integer.toString( input.length));
            try(OutputStream os = con.getOutputStream()) {
                os.write(input, 0, input.length);
            }

            int status = con.getResponseCode();
            if (status == 200) {
                return new String(con.getInputStream().readAllBytes());
            } else {
                throw new RuntimeException("SMART Authentication reading configuration Error status " + status);
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

    private String smartGetTokenUrl(ImmunizationRegistry ir) {
        URL url;
        IParser parser = fhirContext.newJsonParser();
        HttpURLConnection con = null;
        String site_url = ir.getIisFhirUrl().split("/fhir")[0];
        try {
            url = new URL(site_url + "/.well-known/smart-configuration");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            int status = con.getResponseCode();
            if (status == 200) {
                Map<String, Object> response = new Gson().fromJson(new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8), Map.class);
                String token_url = (String) response.get("token_endpoint");
                if (token_url.startsWith("/")) {
                    token_url = site_url + token_url;
                }
                return token_url;
            } else {
                throw new RuntimeException("SMART Authentication reading configuration Error status " + status + " expected 200");
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
