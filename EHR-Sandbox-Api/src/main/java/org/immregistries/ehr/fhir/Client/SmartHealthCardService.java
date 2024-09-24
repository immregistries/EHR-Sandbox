package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    public static final String VC = "vc";

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;

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


    public String parseVCFromCompactJwt(Authentication authentication, String compact) {
        PublicKey publicKey = jwtUtils.getUserPublicKey(authentication);
        return parseVCFromCompactJwt(publicKey, compact);
    }

    public String parseVCFromCompactJwt(PublicKey publicKey, String compact) {
        Jwt jwt = Jwts.parser().zip().add(Jwts.ZIP.DEF).add(Jwts.ZIP.GZIP).and().verifyWith(publicKey).build().parseSignedClaims(compact);
        return parseVCFromJwt(jwt);
    }


    public String parseVCFromCompactJwtUnsecure(String compact) {
        Gson gson = new Gson();
        String[] chunks = compact.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String header = new String(decoder.decode(chunks[0]));
        logger.info("header {}", header);
        String payload = new String(decoder.decode(chunks[1]));
        logger.info("payload {}", payload);
        String inflated = rawInflate(payload);
        logger.info("inflated {}", inflated);
        JsonObject jwtPayload = gson.toJsonTree(payload).getAsJsonObject();
        return jwtPayload.getAsJsonObject(VC).toString();
    }


    public String parseVCFromJwt(Jwt jwt) {
        Gson gson = new Gson();
        logger.info("JWT {}", gson.toJson(jwt));
        JsonObject jwtPayload = gson.toJsonTree(jwt.getPayload()).getAsJsonObject();
        String verifiableCredentialFirst = null;
        if (jwtPayload.has(VC)) {
            verifiableCredentialFirst = jwtPayload.getAsJsonObject(VC).toString();
        }
        return verifiableCredentialFirst;
    }


    public String qrCodeRead(String shc) {
        if (!shc.startsWith(SHC_HEADER)) {
            throw new RuntimeException("Not a SmartHealthCard");
        }
        logger.info("test decode encode {}", getDecodedFromQrCode(getEncodedForQrCode("Salut la companie")));
        String encodedForQrCode = shc.substring(shc.lastIndexOf("/") + 1);
        String decodedFromQrCode = getDecodedFromQrCode(encodedForQrCode);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String verifiableCredential = parseVCFromCompactJwt(authentication, decodedFromQrCode);
            return verifiableCredential;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodedFromQrCode;

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

    public static String rawInflate(String deflated) {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(deflated.getBytes());
            byte[] result = new byte[MAXIMUM_DATA_SIZE];
            int resultLength = inflater.inflate(result);
            inflater.end();
            return Base64.getEncoder().encodeToString(result);
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] rawDeflate(String claimsString) {
        byte[] output = new byte[MAXIMUM_DATA_SIZE];
        Deflater deflater = new Deflater();
        deflater.setInput(claimsString.getBytes());
        deflater.finish();
        int compressedDataSize = deflater.deflate(output);
        if (compressedDataSize >= MAXIMUM_DATA_SIZE) {
            throw new InternalErrorException("Resource is too large");
        }
        byte[] deflated = Arrays.copyOfRange(output, 0, compressedDataSize);
        return deflated;
    }
}
