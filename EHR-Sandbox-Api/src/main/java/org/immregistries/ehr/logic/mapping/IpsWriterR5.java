package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Composition;
import org.hl7.fhir.r5.model.Reference;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class IpsWriterR5 implements IIpsWriter {
    public Composition composition(EhrPatient ehrPatient, Facility facility) {
        Composition composition = new Composition();
        composition.addIdentifier(ehrPatient.getMrnEhrIdentifier().toR5());
        composition.setType(new CodeableConcept(new Coding("http://loinc.org", "60591-5", "Patient summary Document")));
//        composition.setSubject()
        composition.setDate(new Date());
        Reference facilityReference = new Reference().setIdentifier(facility.getIdentifiers().stream().findFirst().get().toR5());
        composition.addAuthor(facilityReference);
        composition.addAttester()
                .setMode(new CodeableConcept(new Coding("", "personal", "")))
                .setTime(new Date())
                .setParty(facilityReference);
        composition.setCustodian(facilityReference);
        composition.addSection()
                .setTitle("Immunization History")
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "11369-6", "History of Immunization Narrative")));
        return composition;

    }
}
