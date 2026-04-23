package org.immregistries.ehr.logic.shlink;

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
 * <p>
 * This class provides convenient methods to access and set claim values
 * while internally storing the data within the map structure.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShCardClaims extends LinkedHashMap<String, Object> implements Claims, Serializable {

    private static final String CONVERSION_ERROR_MSG = "Cannot convert existing claim value of type '%s' to desired type " +
            "'%s'. JJWT only converts simple String, Date, Long, Integer, Short and Byte types automatically. " +
            "Anything more complex is expected to be already converted to your desired type by the JSON Deserializer " +
            "implementation. You may specify a custom Deserializer for a JwtParser with the desired conversion " +
            "configuration via the JwtParserBuilder.deserializer() method. " +
            "See https://github.com/jwtk/jjwt#custom-json-processor for more information. If using Jackson, you can " +
            "specify custom claim POJO types as described in https://github.com/jwtk/jjwt#json-jackson-custom-types";

    /**
     * Default no-argument constructor. Required for Jackson deserialization.
     */
    public ShCardClaims() {
        super();
    }

    // Custom Getters and Setters for type-safety and convenience
    public VerifiableCredential getVerifiableCredential() {
        return (VerifiableCredential) get("vc");
    }

    public void setVerifiableCredential(VerifiableCredential verifiableCredential) {
        put("vc", verifiableCredential);
    }

    // Direct implementation of Claims interface methods, leveraging the map
    @Override
    public String getIssuer() {
        return (String) get("iss");
    }

    @Override
    public Date getExpiration() {
        return (Date) get("exp");
    }

    @Override
    public Date getIssuedAt() {
        return (Date) get("iat");
    }

    @Override
    public Date getNotBefore() {
        return (Date) get("nbf");
    }

    @Override
    public Set<String> getAudience() {
        return (Set<String>) get("aud");
    }

    @Override
    public String getSubject() {
        return (String) get("sub");
    }

    @Override
    public String getId() {
        return (String) get("jti");
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
        put("iss", iss);
        return this;
    }

    public Claims setExpiration(Date exp) {
        put("exp", exp);
        return this;
    }

    public Claims setIssuedAt(Date iat) {
        put("iat", iat);
        return this;
    }

    public Claims setNotBefore(Date nbf) {
        put("nbf", nbf);
        return this;
    }

    public Claims setAudience(Set<String> aud) {
        put("aud", aud);
        return this;
    }

    public Claims setSubject(String sub) {
        put("sub", sub);
        return this;
    }

    public Claims setId(String jti) {
        put("jti", jti);
        return this;
    }

    public Claims set(String claimName, Object value) {
        put(claimName, value);
        return this;
    }


    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Necessary to handle deserialization
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return
     */
    @Override
    public Object put(String key, Object value) {
        if ("vc".equals(key) && value instanceof Map) {
            // Convert the generic Map into your typed POJO
            return super.put(key, mapper.convertValue(value, VerifiableCredential.class));
        }
        return super.put(key, value);
    }

    // Nested classes as per the provided structure

    public static class VerifiableCredential implements Serializable {
        @JsonProperty("type")
        private List<String> type;

        @JsonProperty("credentialSubject")
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
