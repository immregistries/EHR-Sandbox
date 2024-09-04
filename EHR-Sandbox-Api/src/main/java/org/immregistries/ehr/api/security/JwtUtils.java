package org.immregistries.ehr.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.MacAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;

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

    public String qrCodeEncoder(Authentication authentication, Claims claims, byte[] content) {
        final BytesKeyGenerator generator = KeyGenerators.secureRandom(32);
        // Used as Key id
        final byte[] kidBytes = generator.generateKey(); //TODO Sandbox deploy url for key to be available at
        try {
            final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
            final KeyPair keyPair = kpg.generateKeyPair();

            final ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
            final ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            logger.info("Public key {}", publicKey);

            UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
            JwtBuilder jwtBuilder = Jwts.builder()
                    .header().add("use", "SIG")
                    .keyId(Base64.getEncoder().encodeToString(kidBytes))
                    .and()
                    .content(content)
                    .signWith(privateKey);
            logger.info("JWTUTILS content {}", jwtBuilder.compact());
            String compact = jwtBuilder.compact();
            Jwt parsed = Jwts.parser().verifyWith(publicKey).build().parse(compact);
            logger.info("JWTUTILS parsed {}", parsed);
//            byte[] payload = (byte[]) parsed.getPayload();
//            Inflater decompresser = new Inflater();
//            decompresser.setInput(payload, 0, payload.length);
//            byte[] result = new byte[100];
//            try {
//                int resultLength = decompresser.inflate(result);
//                logger.info("result {}", result);
//
//            } catch (DataFormatException e) {
//                throw new RuntimeException(e);
//            }
//            decompresser.end();

            return compact;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            throw new RuntimeException(ex);
        }
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

    private void init() {
        if (key == null) {
            this.key = new SecretKeySpec(DatatypeConverter.parseBase64Binary(jwtSecret), SIGNATURE_ALGORITHM_NAME);
        }
    }
}