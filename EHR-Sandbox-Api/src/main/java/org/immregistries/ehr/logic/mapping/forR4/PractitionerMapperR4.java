package org.immregistries.ehr.logic.mapping.forR4;

import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.immregistries.ehr.logic.mapping.interfaces.IPractitionerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PractitionerMapperR4 implements IPractitionerMapper<Practitioner> {

    @Autowired
    private MappingHelperR4 mappingHelperR4;

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
        if (name.hasSuffix()) {
            clinician.setNameSuffix(name.getSuffixAsSingleString());
        }
        if (name.hasPrefix()) {
            clinician.setNamePrefix(name.getPrefixAsSingleString());
        }

        for (ContactPoint telecom : practitioner.getTelecom()) {
            if (null != telecom.getSystem()) {
                if (telecom.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
                    clinician.addPhoneNumber(mappingHelperR4.toEhrPhoneNumber(telecom));
                }
            }
        }

        for (Identifier identifier : practitioner.getIdentifier()) {
            clinician.addIdentifier(new EhrIdentifier(identifier));
        }

        for (Address address : practitioner.getAddress()) {
            clinician.addAddress(mappingHelperR4.toEhrAddress(address));
        }
        return clinician;
    }

    public Practitioner toFhir(Clinician clinician) {
        Practitioner practitioner = new Practitioner();
        HumanName name = practitioner.addName()
                .addGiven(clinician.getNameFirst())
                .addGiven(clinician.getNameMiddle())
                .setFamily(clinician.getNameLast());
        name.addPrefix(clinician.getNamePrefix());
        name.addSuffix(clinician.getNameSuffix());

        if (clinician.getQualification() != null) {
            practitioner.addQualification().setCode(new CodeableConcept(new Coding().setCode(clinician.getQualification()).setSystem(QUALIFICATION_SYSTEM)));
        }
        for (EhrAddress ehrAddress : clinician.getAddresses()) {
            practitioner.addAddress(mappingHelperR4.toFhirAddress(ehrAddress));
        }

        for (EhrIdentifier ehrIdentifier : clinician.getIdentifiers()) {
            practitioner.addIdentifier(ehrIdentifier.toR4());
        }

        for (EhrPhoneNumber phoneNumber : clinician.getPhones()) {
            practitioner.addTelecom(mappingHelperR4.toFhirContact(phoneNumber));
        }

        return practitioner;
    }
}
