package org.immregistries.ehr.api.entities.embedabbles;


import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.Embeddable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;

import static org.immregistries.ehr.fhir.FhirComponentsService.R4_FLAVOUR;
import static org.immregistries.ehr.fhir.FhirComponentsService.R5_FLAVOUR;
import static org.immregistries.ehr.logic.mapping.IPatientMapper.MRN_TYPE_SYSTEM;
import static org.immregistries.ehr.logic.mapping.IPatientMapper.MRN_TYPE_VALUE;

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

    public EhrIdentifier() {
    }

    public EhrIdentifier(org.hl7.fhir.r5.model.Identifier identifier) {
        system = identifier.getSystem();
        value = identifier.getValue();
        type = identifier.getType().getCode(MRN_TYPE_SYSTEM);
    }

    public EhrIdentifier(org.hl7.fhir.r4.model.Identifier identifier) {
        system = identifier.getSystem();
        value = identifier.getValue();
        type = identifier.getType().getCodingFirstRep().getCode();
    }

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
        if (this.getType() != null) {
            identifier.setType(new org.hl7.fhir.r5.model.CodeableConcept(
                    new org.hl7.fhir.r5.model.Coding(MRN_TYPE_SYSTEM, MRN_TYPE_VALUE, "")));
        }
//        if (assignerIdentifier != null) {
//            identifier.setAssigner(new org.hl7.fhir.r5.model.Reference(assignerReference).setIdentifier(assignerIdentifier.toR5()));
//        } else {
//            identifier.setAssigner(new org.hl7.fhir.r5.model.Reference(assignerReference));
//        }
        return identifier;
    }

    public org.hl7.fhir.r4.model.Identifier toR4() {
        org.hl7.fhir.r4.model.Identifier identifier = new org.hl7.fhir.r4.model.Identifier().setValue(value).setSystem(system);
        if (this.getType() != null) {
            identifier.setType(new org.hl7.fhir.r4.model.CodeableConcept(
                    new org.hl7.fhir.r4.model.Coding(MRN_TYPE_SYSTEM, MRN_TYPE_VALUE, "")));
        }
//        if (assignerIdentifier != null) {
//            identifier.setAssigner(new org.hl7.fhir.r4.model.Reference(assignerReference).setIdentifier(assignerIdentifier.toR4()));
//        } else {
//            identifier.setAssigner(new org.hl7.fhir.r4.model.Reference(assignerReference));
//
//        }
        return identifier;
    }

    public ICompositeType toFhir() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String tenantName = (String) request.getAttribute("TENANT_NAME");
            if (StringUtils.isBlank(tenantName)) {
                return toR5();
            } else if (tenantName.contains(R5_FLAVOUR)) {
                return toR5();
            } else if (tenantName.contains(R4_FLAVOUR)) {
                return toR4();
            } else return toR5();
        } catch (IllegalStateException illegalStateException) {
            return toR5();
        }

    }

    public String getAssignerReference() {
        return assignerReference;
    }

    public void setAssignerReference(String assignerReference) {
        this.assignerReference = assignerReference;
    }
}
