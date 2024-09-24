package org.immregistries.ehr.api.security;

import com.google.gson.Gson;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import io.jsonwebtoken.security.MacAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    @Value("${ehr.api.app.jwtSecret}")
    private String jwtSecret;
    @Value("${ehr.api.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    private final static MacAlgorithm SIGNATURE_ALGORITHM = Jwts.SIG.HS512;
    private final static String SIGNATURE_ALGORITHM_NAME = "HmacSha512";
    private SecretKey key;

    private Map<Integer, String> userKeyIdMap = new HashMap<>(10);
    private Map<String, PublicKey> publicKeyMap = new HashMap<>(10);
    private Map<String, PrivateKey> privateKeyMap = new HashMap<>(10);
//    private KeyStore keyStore = new KeyStore();


    public String generateJwtToken(Authentication authentication) {
        init();
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SIGNATURE_ALGORITHM)
                .compact();
    }

    public String signForQrCode(Authentication authentication, byte[] content) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal(); //TODO Sandbox deploy url for key to be available at
        String kid = getUserKidOrGenerate(authentication);
        PrivateKey privateKey = getUserPrivateKey(authentication);
        logger.info("Issuer key: {}", new Gson().toJson(getUserPrivateJwk(authentication)));
        JwtBuilder jwtBuilder = Jwts.builder()
                .header()
                .add("use", "SIG")
                .add("zip", "DEF")
                .add("test", "12345")
                .keyId(kid)
                .and()
                .content(content)
//                .compressWith(Jwts.ZIP.DEF)
                .signWith(privateKey);
        return jwtBuilder.compact();
    }

    public String getUserNameFromJwtToken(String token) {
        init();
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        init();
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }
        return false;
    }

    public PublicKey getUserPublicKey(Authentication authentication) {
        String kid = getUserKidOrGenerate(authentication);
        return publicKeyMap.get(kid);
    }

    public PrivateKey getUserPrivateKey(Authentication authentication) {
        String kid = getUserKidOrGenerate(authentication);
        return privateKeyMap.get(kid);
    }

    public Jwk getUserPublicJwk(Authentication authentication) {
        String kid = getUserKidOrGenerate(authentication);
        ECPublicKey publicKey = (ECPublicKey) publicKeyMap.get(kid);
        return Jwks.builder()
                .key(publicKey)
                .algorithm("ES256")
                .id(kid)
                .publicKeyUse("sig")
                .build();
    }

    public Jwk getUserPrivateJwk(Authentication authentication) {
        String kid = getUserKidOrGenerate(authentication);
        ECPrivateKey privateKey = (ECPrivateKey) privateKeyMap.get(kid);
        return Jwks.builder()
                .key(privateKey)
                .algorithm("ES256")
                .id(kid).publicKeyUse("sig").build();
    }

    public String getUserKidOrGenerate(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        String kid = userKeyIdMap.get(userPrincipal.getId());
        if (kid == null) {
            kid = createUserKeyPairEC(authentication);
        }
        return kid;
    }

    private String createUserKeyPairEC(Authentication authentication) {
        try {
            String kid = UUID.randomUUID().toString();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
            KeyPair keyPair = kpg.generateKeyPair();
            UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
            ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
            userKeyIdMap.put(userPrincipal.getId(), kid);
            publicKeyMap.put(kid, keyPair.getPublic());
            privateKeyMap.put(kid, keyPair.getPrivate());
            return kid;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void init() {
        if (key == null) {
            this.key = new SecretKeySpec(DatatypeConverter.parseBase64Binary(jwtSecret), SIGNATURE_ALGORITHM_NAME);
        }
    }
}