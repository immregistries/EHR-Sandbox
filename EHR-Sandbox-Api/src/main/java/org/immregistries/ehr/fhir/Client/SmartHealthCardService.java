package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.security.JwtUtils;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
public class SmartHealthCardService {
    private static final Logger logger = LoggerFactory.getLogger(SmartHealthCardService.class);

    private static final int MAX_SINGLE_JWS_SIZE = 1195;
    private static final int MAX_CHUNK_SIZE = 1191;

    public static final int MAXIMUM_DATA_SIZE = 30000;
    private static final int SMALLEST_B64_CHAR_CODE = 45;
    public static final String VERIFIABLE_CREDENTIAL = "VerifiableCredential";
    public static final String HTTPS_SMARTHEALTH_CARDS_HEALTH_CARD = "https://smarthealth.cards#health-card";
    public static final String HTTPS_SMARTHEALTH_CARDS_IMMUNIZATION = "https://smarthealth.cards#immunization";
    public static final String FHIR_VERSION = "fhirVersion";
    public static final String TYPE = "type";
    public static final String FHIR_BUNDLE = "fhirBundle";
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";
    public static final String SHC_HEADER = "shc:/";
    public static final String ISSUER_KEY = "issuerKey";

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;

    public ResponseEntity<List<String>> qrCode(Facility facility, String resourceString, HttpServletRequest request) {
        Map<String, Object> mapVc = new HashMap<>(2);
        ArrayList<String> type = new ArrayList<>(3);
        type.add(VERIFIABLE_CREDENTIAL);
        type.add(HTTPS_SMARTHEALTH_CARDS_HEALTH_CARD);
        type.add(HTTPS_SMARTHEALTH_CARDS_IMMUNIZATION);
        mapVc.put(TYPE, type);

        Map<String, Object> credentialSubject = new HashMap<>(2);
        credentialSubject.put(FHIR_VERSION, fhirComponentsDispatcher.fhirContext().getVersion().getVersion().getFhirVersionString());
        credentialSubject.put(FHIR_BUNDLE, JsonParser.parseString(resourceString).getAsJsonObject());
        mapVc.put(CREDENTIAL_SUBJECT, credentialSubject);

        String issuerUrl = request.getRequestURL().substring(0, request.getRequestURL().indexOf("/tenants"));
        Claims claims = Jwts.claims()
                .notBefore(new Date())
                .issuer(issuerUrl)
                .issuedAt(new Date())
                .add("vc", mapVc)
                .build();

        Gson gson = new Gson();
        String claimsString = gson.toJson(claims).strip();
        /**
         * Compressing the content
         */
        byte[] output = new byte[MAXIMUM_DATA_SIZE];
        Deflater deflater = new Deflater();
        deflater.setInput(claimsString.getBytes());
        deflater.finish();
        int compressedDataSize = deflater.deflate(output);
        if (compressedDataSize >= MAXIMUM_DATA_SIZE) {
            throw new InternalErrorException("Resource is too large");
        }
        byte[] deflated = Arrays.copyOfRange(output, 0, compressedDataSize);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String compact = jwtUtils.signForQrCode(authentication, deflated);
        logger.info("compact {}", compact);

        // for download file
//        Map<String, ArrayList<String>> shcMap = new HashMap<>(1);
//        ArrayList<String> arrayList = new ArrayList<>(1);
//        arrayList.add(compact);
//        shcMap.put("verifiableCredential", arrayList);
//        logger.info("shcMap: {}", shcMap);
        String encodedForQrCode = compact.
                chars().map(value -> value - SMALLEST_B64_CHAR_CODE)
                .boxed()
                .map(integer -> String.valueOf(integer / 10) + integer % 10)
                .collect(Collectors.joining());
        int finalLength = encodedForQrCode.length();
        List<String> result;
        if (finalLength < MAX_SINGLE_JWS_SIZE) {
            result = List.of(SHC_HEADER + encodedForQrCode);
        } else {
            int chunkNumber;
            chunkNumber = finalLength % MAX_CHUNK_SIZE > 0 ? finalLength / MAX_CHUNK_SIZE + 1 : finalLength / MAX_CHUNK_SIZE;
            result = new ArrayList<>(chunkNumber);
            int chunkSize = finalLength / chunkNumber;
            int i = 1;
            while (i < chunkNumber - 1) {
                result.add(SHC_HEADER + i + "/" + chunkNumber + "/" +
                        encodedForQrCode.substring((i - 1) * chunkSize, i * chunkSize));
                i++;
            }
            result.add(SHC_HEADER + i + "/" + chunkNumber + "/" +
                    encodedForQrCode.substring((i - 1) * chunkSize, finalLength - 1));
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(1);
        headers.add(ISSUER_KEY, jwtUtils.getUserPublicJwk(authentication).toString());
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    public String inflate(String deflated) {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(deflated.getBytes());
            byte[] result = new byte[MAXIMUM_DATA_SIZE];
            int resultLength = inflater.inflate(result);
            inflater.end();
            logger.info("result {} {} ", resultLength,
                    Base64.getEncoder().encodeToString(result));
            return Base64.getEncoder().encodeToString(result);
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
    }


    public String parseVerifiableCredentialFromJwt(Authentication authentication, String compact) {
        PublicKey publicKey = jwtUtils.getUserKeyPair(authentication).getPublic();
        return parseVerifiableCredentialFromJwt(publicKey, compact);
    }

    public String parseVerifiableCredentialFromJwt(PublicKey publicKey, String compact) {
        Gson gson = new Gson();
        Jwt jwt = Jwts.parser().verifyWith(publicKey).build().parse(compact);
        JsonObject jwtPayload = gson.toJsonTree(jwt.getPayload()).getAsJsonObject();
        String verifiableCredentialFirst = jwtPayload.getAsJsonArray(VERIFIABLE_CREDENTIAL).get(0).getAsString();
        return inflate(verifiableCredentialFirst);
    }

}
