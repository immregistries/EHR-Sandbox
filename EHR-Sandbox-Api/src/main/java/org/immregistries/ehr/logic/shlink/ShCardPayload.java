package org.immregistries.ehr.logic.shlink;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.hl7.fhir.instance.model.api.IBaseBundle;

import java.io.Serializable;
import java.util.List;


public class ShCardPayload implements Serializable {
    @JsonProperty("iss")
    private String iss;

    @JsonProperty("nbf")
    private long nbf;

    @JsonProperty("vc")
    private VerifiableCredential vc;

    // Constructors, getters, and setters

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public long getNbf() {
        return nbf;
    }

    public void setNbf(long nbf) {
        this.nbf = nbf;
    }

    public VerifiableCredential getVc() {
        return vc;
    }

    public void setVc(VerifiableCredential vc) {
        this.vc = vc;
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