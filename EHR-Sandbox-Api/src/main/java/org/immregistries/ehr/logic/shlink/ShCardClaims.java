package org.immregistries.ehr.logic.shlink;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.impl.DefaultClaims;
import org.hl7.fhir.instance.model.api.IBaseBundle;

import java.io.Serializable;
import java.util.List;


public class ShCardClaims extends DefaultClaims implements Serializable {
//    @JsonProperty("iss")
//    private String issuer;
//
//    @JsonProperty("nbf")
//    private long notBefore;

    @JsonProperty("vc")
    private VerifiableCredential verifiableCredential;

//    public String getIssuer() {
//        return issuer;
//    }
//
//    public void setIssuer(String issuer) {
//        this.issuer = issuer;
//    }
//
//    public long getNotBefore() {
//        return notBefore;
//    }
//
//    public void setNotBefore(long notBefore) {
//        this.notBefore = notBefore;
//    }

    public VerifiableCredential getVerifiableCredential() {
        return verifiableCredential;
    }

    public void setVerifiableCredential(VerifiableCredential verifiableCredential) {
        this.verifiableCredential = verifiableCredential;
    }


    public static class VerifiableCredential implements Serializable {
        @JsonProperty("type")
        private List<String> type;

        @JsonProperty("credentialSubject")
        private CredentialSubject credentialSubject;

        // Constructors, getters, and setters

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
            private IBaseBundle fhirBundle;

            // Constructors, getters, and setters

            public String getFhirVersion() {
                return fhirVersion;
            }

            public void setFhirVersion(String fhirVersion) {
                this.fhirVersion = fhirVersion;
            }

            public IBaseBundle getFhirBundle() {
                return fhirBundle;
            }

            public void setFhirBundle(IBaseBundle fhirBundle) {
                this.fhirBundle = fhirBundle;
            }


        }
    }
}