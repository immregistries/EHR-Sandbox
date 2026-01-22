package org.immregistries.ehr.fhir.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.util.Base64URL;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.security.JwtUtils;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.BundleImportServiceR4;
import org.immregistries.ehr.logic.BundleImportServiceR5;
import org.immregistries.ehr.logic.IBundleImportService;
import org.immregistries.ehr.logic.mapping.MappingHelper;
import org.immregistries.ehr.logic.mapping.forR4.ImmunizationMapperR4;
import org.immregistries.ehr.logic.mapping.forR5.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.interfaces.IImmunizationMapper;
import org.immregistries.ehr.logic.shlink.ShCardClaims;
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
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

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
    public static final String VC = "vc";

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private BundleImportServiceR5 bundleImportServiceR5;
    @Autowired
    private BundleImportServiceR4 bundleImportServiceR4;
    @Autowired
    private ImmunizationMapperR5 immunizationMapperR5;
    @Autowired
    private ImmunizationMapperR4 immunizationMapperR4;

    public ResponseEntity<List<String>> qrCodeWrite(Facility facility, String resourceString, HttpServletRequest request) {
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
                .add(VC, mapVc)
                .build();

        Gson gson = new Gson();
        String claimsString = gson.toJson(claims).strip();
        /**
         * Compressing the content
         */
        byte[] deflated = rawDeflate(claimsString);

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
            result = List.of(SHC_HEADER + encodedForQrCode);
        } else {
            int numberOfChunks = finalLength / MAX_CHUNK_SIZE;
            if (finalLength % MAX_CHUNK_SIZE > 0) {
                numberOfChunks += 1;
            }
            result = new ArrayList<>(numberOfChunks);
            int chunkSize = finalLength / numberOfChunks;
            for (int i = 1; i < numberOfChunks; i++) {
                result.add(SHC_HEADER + i + "/" + numberOfChunks + "/" + encodedForQrCode.substring((i - 1) * chunkSize, i * chunkSize));
            }
            result.add(SHC_HEADER + numberOfChunks + "/" + numberOfChunks + "/" +
                    encodedForQrCode.substring((numberOfChunks - 1) * chunkSize, finalLength - 1));
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(1);
        headers.add(ISSUER_KEY, gson.toJsonTree(jwtUtils.getUserPrivateJwk(authentication)).toString());
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }


    public ShCardClaims.VerifiableCredential parseVCFromCompactJwt(Authentication authentication, String compact) throws JsonProcessingException {
        PublicKey publicKey = jwtUtils.getUserPublicKey(authentication);
        return parseVCFromCompactJwt(publicKey, compact);
    }

    public ShCardClaims.VerifiableCredential parseVCFromCompactJwt(PublicKey publicKey, String compact) throws CompressionException, JsonProcessingException {
        logger.info("Parsing compact {}\n\n", compact);
        ObjectMapper objectMapper = new ObjectMapper();

        Gson gson = new Gson();
        if (publicKey != null) {
            Jwt jws = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(compact);
            logger.info("Parsing payload {}\n\n", jws.getPayload());


            // Casting or parsing directly doesn't work with claims
            String jsonPayload = gson.toJson(jws.getPayload());
            logger.info("json payload {}", jsonPayload);
            ShCardClaims shCardClaims = objectMapper.readValue(jsonPayload, ShCardClaims.class);
            return shCardClaims.getVerifiableCredential();
        } else {
            Base64URL base64URL = Base64URL.from(StringUtils.substringBetween(compact, "."));
            logger.info("TESTTT {}", new String(base64URL.decode()));
            ShCardClaims shCardClaims = objectMapper.readValue(new String(base64URL.decode()), ShCardClaims.class);
//            return parseVCFromCompactJwtUnsecure(compact);
//            logger.info("PAYLOAD {} {}", shCardClaims, shCardClaims.getIssuer());
            return shCardClaims.getVerifiableCredential();
        }
    }

    public List<VaccinationEvent> parseBundleVaccinationsFromVC(JsonObject vc) throws CompressionException {
        if (vc.has(CREDENTIAL_SUBJECT)) {
            JsonObject credentialSubject = vc.getAsJsonObject(CREDENTIAL_SUBJECT);
            String fhirVersion = credentialSubject.get(FHIR_VERSION).getAsString();
            String fhirBundle = credentialSubject.get(FHIR_BUNDLE).toString();
            logger.info("fhirBundle {}", fhirBundle);
            IBundleImportService iBundleImportService;
            IImmunizationMapper immunizationMapper;
            if (fhirVersion.startsWith("4.0")) {
                iBundleImportService = bundleImportServiceR4;
                immunizationMapper = immunizationMapperR4;
            } else if (fhirVersion.startsWith("5.")) {
                iBundleImportService = bundleImportServiceR5;
                immunizationMapper = immunizationMapperR5;
            } else {
                throw new RuntimeException("Fhir Version not supported " + fhirVersion);
            }
            return extractVaccinationEvents(fhirBundle, iBundleImportService, immunizationMapper);
        }
        return null;
    }

    public List<VaccinationEvent> parseBundleVaccinationsUnknownVersion(String fhirBundle) {
        IBundleImportService iBundleImportService;
        IImmunizationMapper immunizationMapper;
        try {
            iBundleImportService = bundleImportServiceR4;
            immunizationMapper = immunizationMapperR4;
            return extractVaccinationEvents(fhirBundle, iBundleImportService, immunizationMapper);
        } catch (Exception e) {
            e.printStackTrace();
            iBundleImportService = bundleImportServiceR5;
            immunizationMapper = immunizationMapperR5;
            return extractVaccinationEvents(fhirBundle, iBundleImportService, immunizationMapper);
        }
    }

    private static List<VaccinationEvent> extractVaccinationEvents(String fhirBundle, IBundleImportService iBundleImportService, IImmunizationMapper immunizationMapper) {
        List<IDomainResource> list = iBundleImportService.domainResourcesFromBaseBundleEntries(fhirBundle);
        List<VaccinationEvent> vaccinationEvents = new ArrayList<>(5);
        for (IDomainResource iDomainResource : list) {
            if (iDomainResource.fhirType().equals(MappingHelper.IMMUNIZATION)) {
                VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(iDomainResource);
                vaccinationEvents.add(vaccinationEvent);
            }
        }
        return vaccinationEvents;
    }


    public ShCardClaims.VerifiableCredential parseVCFromCompactJwtUnsecure(String compact) throws JsonProcessingException {
        String[] chunks = compact.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        JsonObject header = JsonParser.parseString(new String(decoder.decode(chunks[0]))).getAsJsonObject();
//        logger.info("header {}", header);
        byte[] payload = decoder.decode(chunks[1]);
        String payloadString;
        if (header.has("zip") && header.get("zip").getAsString().equalsIgnoreCase("DEF")) {
            payloadString = rawInflate(payload);
        } else {
            payloadString = new String(payload);
        }
        ObjectMapper objectMapper = new ObjectMapper();

        ShCardClaims shCardClaims = objectMapper.readValue(payloadString, ShCardClaims.class);
        return shCardClaims.getVerifiableCredential();
    }


    public String qrCodeRead(String shc) {
        if (!shc.startsWith(SHC_HEADER)) {
            throw new RuntimeException("Not a SmartHealthCard");
        }
        String encodedForQrCode = shc.substring(shc.lastIndexOf("/") + 1);
        String decodedFromQrCode = getDecodedFromQrCode(encodedForQrCode);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            ObjectMapper objectMapper = new ObjectMapper();
            ShCardClaims.VerifiableCredential verifiableCredential = parseVCFromCompactJwt(authentication, decodedFromQrCode);
            return objectMapper.writer().writeValueAsString(verifiableCredential);
        } catch (Exception e) {
            throw new RuntimeException("Failed to Read Qr Code " + decodedFromQrCode, e);
        }
    }


    private static String getEncodedForQrCode(String compact) {
        String encodedForQrCode = compact.
                chars().map(value -> value - SMALLEST_B64_CHAR_CODE)
                .boxed()
                .map(integer -> String.valueOf(integer / 10) + integer % 10)
                .collect(Collectors.joining());
        return encodedForQrCode;
    }


    private static String getDecodedFromQrCode(String encodedForQrCode) {
        // TODO verify this part , generated from chat gpt
        String decodedFromQrCode = IntStream.range(0, encodedForQrCode.length() / 2)
                .map(i -> {
                    String pair = encodedForQrCode.substring(i * 2, i * 2 + 2);
                    int integer = Integer.parseInt(pair);
                    return (char) (integer + SMALLEST_B64_CHAR_CODE);
                })
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return decodedFromQrCode;
    }

    public static String rawInflate(byte[] deflated) {
        try {
            return new String(CompressionUtil.inflate(deflated));
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] rawDeflate(String claimsString) {
        return CompressionUtil.deflate(claimsString.getBytes());
    }
}
