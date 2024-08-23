package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Practitioner;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class PractitionerMapperR5 implements IPractitionerMapper<Practitioner> {

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
        for (ContactPoint telecom : practitioner.getTelecom()) {
            if (null != telecom.getSystem()) {
                if (telecom.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
                    clinician.addPhoneNumber(MappingHelperR5.toEhrPhoneNumber(telecom));
                }
            }
        }
        for (Address address : practitioner.getAddress()) {
            clinician.addAddress(MappingHelperR5.toEhrAddress(address));
        }
        return clinician;
    }

    public Practitioner toFhir(Clinician clinician) {
        Practitioner practitioner = new Practitioner();
        practitioner.addName()
                .addGiven(clinician.getNameFirst())
                .addGiven(clinician.getNameMiddle())
                .setFamily(clinician.getNameLast());
        for (EhrAddress ehrAddress : clinician.getAddresses()
        ) {
            practitioner.addAddress(MappingHelperR5.toFhirAddress(ehrAddress));
        }

        for (EhrPhoneNumber phoneNumber : clinician.getPhones()) {
            practitioner.addTelecom(MappingHelperR5.toFhirContact(phoneNumber));
        }

        return practitioner;
    }
}
