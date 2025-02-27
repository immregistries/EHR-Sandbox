package org.immregistries.ehr.api.entities.embedabbles;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.immregistries.ehr.api.ProcessingFlavor;

import static org.immregistries.ehr.logic.IFhirTransactionWriter.TYPE_ISO;
import static org.immregistries.ehr.logic.IFhirTransactionWriter.TYPE_UUID;
import static org.immregistries.ehr.logic.mapping.interfaces.IPatientMapper.IDENTIFIER_TYPE_SYSTEM;
import static org.immregistries.ehr.logic.mapping.interfaces.IPatientMapper.MRN_TYPE_VALUE;

@Embeddable()
public class EhrIdentifier {

    @Size(max = 300)
    @Column(name = "identifier_system")
    private String system;

    @Size(max = 300)
    @Column(name = "identifier_value")
    private String value;

    @Size(max = 300)
    @Column(name = "identifier_type")
    private String type;

    @Size(max = 300)
    @Column(name = "identifier_assigner")
    private String assignerReference;

    //    @Embedded
//    private EhrIdentifier assignerIdentifier;

    public EhrIdentifier() {
    }

    public EhrIdentifier(org.hl7.fhir.r5.model.Identifier identifierR5) {
        system = identifierR5.getSystem();
        value = identifierR5.getValue();
        type = identifierR5.getType().getCode(IDENTIFIER_TYPE_SYSTEM);
    }

    public EhrIdentifier(org.hl7.fhir.r4.model.Identifier identifierR4) {
        system = identifierR4.getSystem();
        value = identifierR4.getValue();
        type = identifierR4.getType().getCodingFirstRep().getCode();
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAssignerReference() {
        return assignerReference;
    }

    public void setAssignerReference(String assignerReference) {
        this.assignerReference = assignerReference;
    }

    @Transient
    public org.hl7.fhir.r5.model.Identifier toR5() {
        org.hl7.fhir.r5.model.Identifier identifier = new org.hl7.fhir.r5.model.Identifier().setValue(value).setSystem(system);
        if (this.getType() != null) {
            identifier.setType(new org.hl7.fhir.r5.model.CodeableConcept(
                    new org.hl7.fhir.r5.model.Coding(IDENTIFIER_TYPE_SYSTEM, MRN_TYPE_VALUE, "")));
        }
//        if (assignerIdentifier != null) {
//            identifier.setAssigner(new org.hl7.fhir.r5.model.Reference(assignerReference).setIdentifier(assignerIdentifier.toR5()));
//        } else {
//            identifier.setAssigner(new org.hl7.fhir.r5.model.Reference(assignerReference));
//        }
        return identifier;
    }

    @Transient
    public org.hl7.fhir.r4.model.Identifier toR4() {
        org.hl7.fhir.r4.model.Identifier identifier = new org.hl7.fhir.r4.model.Identifier().setValue(value).setSystem(system);
        if (this.getType() != null) {
            identifier.setType(new org.hl7.fhir.r4.model.CodeableConcept(
                    new org.hl7.fhir.r4.model.Coding(IDENTIFIER_TYPE_SYSTEM, MRN_TYPE_VALUE, "")));
        }
//        if (assignerIdentifier != null) {
//            identifier.setAssigner(new org.hl7.fhir.r4.model.Reference(assignerReference).setIdentifier(assignerIdentifier.toR4()));
//        } else {
//            identifier.setAssigner(new org.hl7.fhir.r4.model.Reference(assignerReference));
//
//        }
        return identifier;
    }

    @Transient
    public ICompositeType toFhir() {
        try {
            if (ProcessingFlavor.R5.isActive()) {
                return toR5();
            } else if (ProcessingFlavor.R4.isActive()) {
                return toR4();
            } else return toR5();
        } catch (IllegalStateException illegalStateException) {
            return toR5();
        }

    }

    @Transient
    public String toV2HD(String separator) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getValue()).append(separator);
        sb.append(this.getValue()).append(separator);
        if (StringUtils.isBlank(this.getType())) {
            sb.append(this.getSystem());
        } else if (TYPE_UUID.equals(this.getType()) || TYPE_ISO.equals(this.getType())) {
            sb.append("urn:ietf:rfc:3986");
        } else {
            sb.append(this.getType());
        }
        return sb.toString();
    }


}
