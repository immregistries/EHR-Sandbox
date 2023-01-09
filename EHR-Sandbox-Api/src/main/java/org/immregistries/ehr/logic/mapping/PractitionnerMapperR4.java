package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Practitioner;
import org.immregistries.ehr.api.entities.Clinician;
import org.springframework.stereotype.Service;

@Service
public class PractitionnerMapperR4 {

    public Clinician fromFhir(Practitioner practitioner) {
        Clinician clinician = new Clinician();
        HumanName name = practitioner.getNameFirstRep();
        clinician.setNameLast(name.getFamily());
        if(name.getGiven().size()>=1) {
            clinician.setNameFirst(name.getGiven().get(0).getValue());
        }
        if(name.getGiven().size()>=2) {
            clinician.setNameMiddle(name.getGiven().get(1).getValue());
        }
        return clinician;
    }

    public Practitioner fromModel(Clinician clinician) {
        Practitioner practitioner = new Practitioner();
        practitioner.addName()
                .addGiven(clinician.getNameFirst())
                .addGiven(clinician.getNameMiddle())
                .setFamily(clinician.getNameLast());
        return practitioner;
    }
}
