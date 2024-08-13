package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Practitioner;
import org.immregistries.ehr.api.entities.Clinician;
import org.springframework.stereotype.Service;

@Service

public class PractitionerMapperR4 implements IPractitionerMapper<Practitioner> {

    public Clinician toClinician(Practitioner practitioner) {
        Clinician clinician = new Clinician();
        HumanName name = practitioner.getNameFirstRep();
        clinician.setNameLast(name.getFamily());
        if (name.getGiven().size() >= 1) {
            clinician.setNameFirst(name.getGiven().get(0).getValue());
        }
        if (name.getGiven().size() >= 2) {
            clinician.setNameMiddle(name.getGiven().get(1).getValue());
        }
        return clinician;
    }

    public Practitioner toFhir(Clinician clinician) {
        Practitioner practitioner = new Practitioner();
        practitioner.addName()
                .addGiven(clinician.getNameFirst())
                .addGiven(clinician.getNameMiddle())
                .setFamily(clinician.getNameLast());
        return practitioner;
    }
}
