package org.immregistries.ehr.shlink.service;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.security.JwtUtils;
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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

import static org.immregistries.ehr.shlink.SmartHealthConstants.*;
import static org.immregistries.ehr.shlink.model.ShCardClaims.VC;

@Service
public class SmartHealthCardWriter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;

    private Gson gson = new Gson();

    public ResponseEntity<List<String>> qrCodeWrite(Facility facility, String resourceString, String issuerUrl) {
        Map<String, Object> mapVc = new HashMap<>(2);
        ArrayList<String> type = new ArrayList<>(3);
        type.add(VERIFIABLE_CREDENTIAL_TYPE);
        type.add(HTTPS_SMARTHEALTH_CARDS_HEALTH_CARD);
        type.add(HTTPS_SMARTHEALTH_CARDS_IMMUNIZATION);
        mapVc.put(TYPE, type);

        Map<String, Object> credentialSubject = new HashMap<>(2);
        credentialSubject.put(FHIR_VERSION, fhirComponentsDispatcher.fhirContext().getVersion().getVersion().getFhirVersionString());
        credentialSubject.put(FHIR_BUNDLE, JsonParser.parseString(resourceString).getAsJsonObject());
        mapVc.put(CREDENTIAL_SUBJECT, credentialSubject);

        Claims claims = Jwts.claims()
                .notBefore(new Date())
                .issuer(issuerUrl)
                .issuedAt(new Date())
                .add(VC, mapVc)
                .build();

        String claimsString = gson.toJson(claims).strip();
        /**
         * Compressing the content
         */
        byte[] deflated = CompressionUtil.deflate(claimsString.getBytes());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String kid = jwtUtils.getUserKidOrGenerate(authentication);
        PrivateKey privateKey = jwtUtils.getUserPrivateKey(authentication);
        /**
         * DEF header added manually as we are using raw deflation
         */
        JwtBuilder jwtBuilder = Jwts.builder()
                .header()
                .add("use", "SIG")
                .add("zip", "DEF")
                .keyId(kid)
                .and()
                .content(deflated)
//                .compressWith(Jwts.ZIP.DEF)
                .signWith(privateKey);
        String compact = jwtBuilder.compact();
        logger.info("compact {}", compact);
        PublicKey publicKey = jwtUtils.getUserPublicKey(authentication);
        logger.info("parsed {}", Jwts.parser().verifyWith(publicKey).build().parse(compact));

        // for download file
//        Map<String, ArrayList<String>> shcMap = new HashMap<>(1);
//        ArrayList<String> arrayList = new ArrayList<>(1);
//        arrayList.add(compact);
//        shcMap.put("verifiableCredential", arrayList);
//        logger.info("shcMap: {}", shcMap);
        String encodedForQrCode = getEncodedForQrCode(compact);
        int finalLength = encodedForQrCode.length();
        List<String> result;
        if (finalLength < MAX_SINGLE_JWS_SIZE) {
            result = List.of(SH_CARD_PREFIX + encodedForQrCode);
        } else {
            int numberOfChunks = finalLength / MAX_CHUNK_SIZE;
            if (finalLength % MAX_CHUNK_SIZE > 0) {
                numberOfChunks += 1;
            }
            result = new ArrayList<>(numberOfChunks);
            int chunkSize = finalLength / numberOfChunks;
            for (int i = 1; i < numberOfChunks; i++) {
                result.add(SH_CARD_PREFIX + i + "/" + numberOfChunks + "/" + encodedForQrCode.substring((i - 1) * chunkSize, i * chunkSize));
            }
            result.add(SH_CARD_PREFIX + numberOfChunks + "/" + numberOfChunks + "/" +
                    encodedForQrCode.substring((numberOfChunks - 1) * chunkSize, finalLength - 1));
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(1);
        headers.add(ISSUER_KEY, gson.toJsonTree(jwtUtils.getUserPrivateJwk(authentication)).toString());
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    private static String getEncodedForQrCode(String compact) {
        String encodedForQrCode = compact.
                chars().map(value -> value - SMALLEST_B64_CHAR_CODE)
                .boxed()
                .map(integer -> String.valueOf(integer / 10) + integer % 10)
                .collect(Collectors.joining());
        return encodedForQrCode;
    }


}
