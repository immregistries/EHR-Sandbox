package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class IpsWriterR4 implements IIpsWriter {
    @Autowired
    PatientMapperR4 patientMapperR4;
    @Autowired
    ImmunizationMapperR4 immunizationMapperR4;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;

    public Composition composition(EhrPatient ehrPatient, Facility facility) {
        Composition composition = new Composition();
        composition.setIdentifier(ehrPatient.getMrnEhrIdentifier().toR4());
        composition.setType(new CodeableConcept(new Coding("http://loinc.org", "60591-5", "Patient summary Document")));
//        composition.setSubject()
        composition.setDate(new Date());
        Reference facilityReference = new Reference().setIdentifier(facility.getIdentifiers().stream().findFirst().get().toR4());
        composition.addAuthor(facilityReference);
        composition.addAttester()
                .setMode(Composition.CompositionAttestationMode.PERSONAL)
                .setTime(new Date())
                .setParty(facilityReference);
        composition.setCustodian(facilityReference);
        composition.addSection()
                .setTitle("Immunization History")
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "11369-6", "History of Immunization Narrative")));
        return composition;
    }

    private Patient ipsPatient(EhrPatient ehrPatient, Facility facility) {
        Patient patient = patientMapperR4.toFhir(ehrPatient, facility);
//        patient.setGeneralPractitioner()
        return patient;
    }


    private Immunization ipsImmunization(VaccinationEvent vaccinationEvent, Facility facility) {
        Immunization immunization = immunizationMapperR4.toFhir(vaccinationEvent, resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility));
        return immunization;
    }
}
