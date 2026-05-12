package org.immregistries.ehr.shlink;

public class SmartHealthConstants {

    public static final int MAX_SINGLE_JWS_SIZE = 1195;
    public static final int MAX_CHUNK_SIZE = 1191;

    public static final int MAXIMUM_DATA_SIZE = 30000;
    public static final int SMALLEST_B64_CHAR_CODE = 45;
    public static final String VERIFIABLE_CREDENTIAL_TYPE = "VerifiableCredential";
    public static final String HTTPS_SMARTHEALTH_CARDS_HEALTH_CARD = "https://smarthealth.cards#health-card";
    public static final String HTTPS_SMARTHEALTH_CARDS_IMMUNIZATION = "https://smarthealth.cards#immunization";
    public static final String FHIR_VERSION = "fhirVersion";
    public static final String TYPE = "type";
    public static final String FHIR_BUNDLE = "fhirBundle";
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";
    public static final String SH_CARD_PREFIX = "shc:/";
    public static final String ISSUER_KEY = "issuerKey";

    public static final Character U_FLAG = 'U';
    public static final Character P_FLAG = 'P';
    public static final Character L_FLAG = 'L';
    public static final String SHLINK_PREFIX = "shlink:/";


    public static final String ALGORITHM = "AES";

    public static class ContentType {
        public static final String APPLICATION_SMART_HEALTH_CARD = "application/smart-health-card";
        public static final String APPLICATION_FHIR_JSON = "application/fhir+json";
        public static final String APPLICATION_SMART_API_ACCESS = "application/smart-api-access";
    }
}
