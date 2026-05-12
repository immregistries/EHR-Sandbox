package org.immregistries.ehr.shlink.model;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.hl7.fhir.instance.model.api.IBaseBundle;

import java.io.Serializable;
import java.util.*;


/**
 * A custom implementation of the {@link Claims} interface that also
 * extends {@link LinkedHashMap}, allowing it to function as a
 * a self-contained representation of the JWT payload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShCardClaims extends LinkedHashMap<String, Object> implements Claims, Serializable {

    // Standard JWT Claim Keys
    public static final String ISS = "iss";
    public static final String SUB = "sub";
    public static final String AUD = "aud";
    public static final String EXP = "exp";
    public static final String NBF = "nbf";
    public static final String IAT = "iat";
    public static final String JTI = "jti";
    public static final String VC = "vc";

    private static final String CONVERSION_ERROR_MSG = "Cannot convert existing claim value of type '%s' to desired type " +
            "'%s'. JJWT only converts simple String, Date, Long, Integer, Short and Byte types automatically. ";

    private static final ObjectMapper mapper = new ObjectMapper();

    public ShCardClaims() {
        super();
    }

    // Custom Getters and Setters
    public VerifiableCredential getVerifiableCredential() {
        return (VerifiableCredential) get(VC);
    }

    public void setVerifiableCredential(VerifiableCredential verifiableCredential) {
        put(VC, verifiableCredential);
    }

    // Claims interface implementation
    @Override
    public String getIssuer() {
        return (String) get(ISS);
    }

    @Override
    public Date getExpiration() {
        return (Date) get(EXP);
    }

    @Override
    public Date getIssuedAt() {
        return (Date) get(IAT);
    }

    @Override
    public Date getNotBefore() {
        return (Date) get(NBF);
    }

    @Override
    public Set<String> getAudience() {
        return (Set<String>) get(AUD);
    }

    @Override
    public String getSubject() {
        return (String) get(SUB);
    }

    @Override
    public String getId() {
        return (String) get(JTI);
    }

    @Override
    public <T> T get(String claimName, Class<T> requiredType) {
        Object value = get(claimName);
        if (value != null && requiredType.isInstance(value)) {
            return requiredType.cast(value);
        }
        return null;
    }

    public Map<String, Object> asMap() {
        return this;
    }

    public Claims setIssuer(String iss) {
        put(ISS, iss);
        return this;
    }

    public Claims setExpiration(Date exp) {
        put(EXP, exp);
        return this;
    }

    public Claims setIssuedAt(Date iat) {
        put(IAT, iat);
        return this;
    }

    public Claims setNotBefore(Date nbf) {
        put(NBF, nbf);
        return this;
    }

    public Claims setAudience(Set<String> aud) {
        put(AUD, aud);
        return this;
    }

    public Claims setSubject(String sub) {
        put(SUB, sub);
        return this;
    }

    public Claims setId(String jti) {
        put(JTI, jti);
        return this;
    }

    public Claims set(String claimName, Object value) {
        put(claimName, value);
        return this;
    }

    @Override
    public Object put(String key, Object value) {
        if (VC.equals(key) && value instanceof Map) {
            return super.put(key, mapper.convertValue(value, VerifiableCredential.class));
        }
        return super.put(key, value);
    }

    public static class VerifiableCredential implements Serializable {
        public static final String TYPE = "type";
        public static final String CREDENTIAL_SUBJECT = "credentialSubject";
        @JsonProperty(TYPE)
        private List<String> type;

        @JsonProperty(CREDENTIAL_SUBJECT)
        private CredentialSubject credentialSubject;

        public List<String> getType() {
            return type;
        }

        public void setType(List<String> type) {
            this.type = type;
        }

        public CredentialSubject getCredentialSubject() {
            return credentialSubject;
        }

        public void setCredentialSubject(CredentialSubject credentialSubject) {
            this.credentialSubject = credentialSubject;
        }

        public static class CredentialSubject implements Serializable {
            @JsonProperty("fhirVersion")
            private String fhirVersion;

            @JsonProperty("fhirBundle")
            private JsonNode fhirBundle;

            public String getFhirVersion() {
                return fhirVersion;
            }

            public void setFhirVersion(String fhirVersion) {
                this.fhirVersion = fhirVersion;
            }

            public JsonNode getFhirBundle() {
                return fhirBundle;
            }

            public void setFhirBundle(JsonNode fhirBundle) {
                this.fhirBundle = fhirBundle;
            }

            @JsonIgnore
            public IBaseBundle parseBundle(FhirContext ctx) {
                if (this.fhirBundle == null) return null;
                return (IBaseBundle) ctx.newJsonParser().parseResource(this.fhirBundle.toString());
            }
        }
    }
}