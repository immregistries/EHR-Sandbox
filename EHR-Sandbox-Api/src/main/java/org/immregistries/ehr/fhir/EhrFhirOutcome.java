package org.immregistries.ehr.fhir;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;

public class EhrFhirOutcome {
    private String id = "";
    private String operationOutcome = "";
    private String errorMessage = "";

    public EhrFhirOutcome() {
    }

    private EhrFhirOutcome(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static EhrFhirOutcome fromMethodOutcome(MethodOutcome methodOutcome, IParser parser) {
        EhrFhirOutcome ehrFhirOutcome = new EhrFhirOutcome();
        ehrFhirOutcome.setId(methodOutcome.getId().getValue());
        ehrFhirOutcome.setOperationOutcome(parser.encodeResourceToString(methodOutcome.getOperationOutcome()));
        return ehrFhirOutcome;
    }

    public static EhrFhirOutcome error(String errorMessage) {
        return new EhrFhirOutcome(errorMessage);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperationOutcome() {
        return operationOutcome;
    }

    public void setOperationOutcome(String operationOutcome) {
        this.operationOutcome = operationOutcome;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
