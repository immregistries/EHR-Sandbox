package org.immregistries.ehr.api.entities.embedabbles;


import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable()
public class EhrIdentifier {

    @Size(max = 300)
    private String system;

    @Size(max = 300)
    private String value;

    @Size(max = 300)
    private String type;

    @Size(max = 300)
    private String assignerReference;

//    @Embedded
//    private EhrIdentifier assignerIdentifier;


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

    public org.hl7.fhir.r5.model.Identifier toR5() {
        org.hl7.fhir.r5.model.Identifier identifier = new org.hl7.fhir.r5.model.Identifier().setValue(value).setSystem(system);
//        if (assignerIdentifier != null) {
//            identifier.setAssigner(new org.hl7.fhir.r5.model.Reference(assignerReference).setIdentifier(assignerIdentifier.toR5()));
//        } else {
//            identifier.setAssigner(new org.hl7.fhir.r5.model.Reference(assignerReference));
//
//        }
        return identifier;
    }

    public org.hl7.fhir.r4.model.Identifier toR4() {
        org.hl7.fhir.r4.model.Identifier identifier = new org.hl7.fhir.r4.model.Identifier().setValue(value).setSystem(system);
//        if (assignerIdentifier != null) {
//            identifier.setAssigner(new org.hl7.fhir.r4.model.Reference(assignerReference).setIdentifier(assignerIdentifier.toR4()));
//        } else {
//            identifier.setAssigner(new org.hl7.fhir.r4.model.Reference(assignerReference));
//
//        }
        return identifier;
    }
}
