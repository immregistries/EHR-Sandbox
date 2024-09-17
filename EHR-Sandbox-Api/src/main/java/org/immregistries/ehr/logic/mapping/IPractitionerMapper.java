package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

import static org.immregistries.ehr.logic.ResourceIdentificationService.CLINICIAN_SYSTEM;

public interface IPractitionerMapper<Practitioner extends IBaseResource> extends IEhrEntityFhirMapper<Clinician> {
    String QUALIFICATION_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0360";

    Practitioner toFhir(Clinician clinician);

    Clinician toClinician(Practitioner practitioner);

    static EhrIdentifier clinicianEhrIdentifier(Clinician clinician) {
        if (clinician.getIdentifiers().isEmpty()) { // TODO find a better solution and better system
            EhrIdentifier ehrIdentifier = new EhrIdentifier();
            ehrIdentifier.setSystem(CLINICIAN_SYSTEM);
            ehrIdentifier.setValue(clinician.getId());
            return ehrIdentifier;
        } else {
            return clinician.getIdentifiers().stream().findFirst().get();
        }

    }
}
