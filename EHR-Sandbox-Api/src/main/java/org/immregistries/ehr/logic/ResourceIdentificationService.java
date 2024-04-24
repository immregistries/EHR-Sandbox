package org.immregistries.ehr.logic;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Scanner;

import static org.immregistries.ehr.logic.mapping.PatientMapperR5.MRN_SYSTEM;

@Service
public class ResourceIdentificationService {
    Logger logger = LoggerFactory.getLogger(ResourceIdentificationService.class);
    public static final String FACILITY_SYSTEM = "ehr-sandbox/facility";
    private static final String IDENTIFIER_SYSTEM_PREFIX = FACILITY_SYSTEM + "/";
    private static final String PATIENT_IDENTIFIER_SYSTEM_SUFFIX = "/patient-system";
    private static final String IMMUNIZATION_IDENTIFIER_SYSTEM_SUFFIX = "/immunization-system";

    /**
     *
     * Defines a system for FHIR identifier system in the facility
     * @param facility
     * @return
     */
    public String getFacilityPatientIdentifierSystem(Facility facility) {
        return IDENTIFIER_SYSTEM_PREFIX + facility.getId() + PATIENT_IDENTIFIER_SYSTEM_SUFFIX;
    }

    public String getFacilityImmunizationIdentifierSystem(Facility facility) {
        return IDENTIFIER_SYSTEM_PREFIX + facility.getId() + IMMUNIZATION_IDENTIFIER_SYSTEM_SUFFIX;
    }
    @Autowired
    PatientIdentifierRepository patientIdentifierRepository;
    @Autowired
    ImmunizationIdentifierRepository immunizationIdentifierRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;


    public String getPatientLocalId(Patient remotePatient, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String id;
        /**
         * first we check if the patient has known identifier for the facility system or MRN
         */
        for (Identifier identifier: remotePatient.getIdentifier()) {
            id = getPatientLocalId(identifier, facility);
            if ( id != null && !id.isBlank()) {
                return id;
            }
        }
        /**
         * if not we check if ID is known is external identifier registry
         */
        return getPatientLocalId(new IdType(remotePatient.getId()), immunizationRegistry);
    }

    public String getPatientLocalId(Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String localId = null;
        if (reference.getIdentifier() != null){
            localId = getPatientLocalId(reference.getIdentifier(), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank()) {
            localId = getPatientLocalId(new IdType(reference.getReference()), immunizationRegistry);
        }
        return  localId;
    }

    public String getPatientLocalId(IdType idType, ImmunizationRegistry immunizationRegistry) {
        return patientIdentifierRepository.findByIdentifierAndImmunizationRegistryId(idType.getIdPart(),immunizationRegistry.getId())
                .orElse(new PatientExternalIdentifier()).getPatientId();
    }

    public String getPatientLocalId(Identifier identifier, Facility facility) {
        logger.info("Reference identifier {} {}", identifier.getSystem(), identifier.getValue());
        logger.info("Facility identifier  system {} {}", getFacilityPatientIdentifierSystem(facility), identifier.getSystem().equals(getFacilityPatientIdentifierSystem(facility)));
        if (identifier.getSystem().equals(getFacilityPatientIdentifierSystem(facility))) {
            return ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), identifier.getValue())
                    .map(EhrPatient::getId).orElse(identifier.getValue()); // TODO Decide on what to do with this kind of identifier
        } else if (StringUtils.isBlank(identifier.getSystem())){
            return ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), identifier.getValue())
                    .map(EhrPatient::getId).orElse(null);
        } else if (identifier.getSystem().equals(MRN_SYSTEM)) {
            return ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), identifier.getValue())
                    .map(EhrPatient::getId).orElse(null);
        }  else {
            return null;
        }
    }


    public String getImmunizationLocalId(Immunization remoteImmunization, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String id = "";
        /**
         * first we check if the patient has known identifier for the facility system
         */
        for (Identifier identifier: remoteImmunization.getIdentifier()) {
            id = getImmunizationLocalId(identifier, facility);
            if ( id != null && !id.isBlank()) {
                return id;
            }
        }
        /**
         * if not we check if id is known is external identifier registry
         */
        return getImmunizationLocalId(new IdType(remoteImmunization.getId()), immunizationRegistry);
    }


    public String getImmunizationLocalId(Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        if (reference.getReference() != null && !reference.getReference().isBlank()) {
            return getImmunizationLocalId(new IdType(reference.getReference()), immunizationRegistry);
        } else if (reference.getIdentifier() != null){
            return getImmunizationLocalId(reference.getIdentifier(), facility);
        } else {
            return null;
        }
    }
    public String getImmunizationLocalId(IdType idType, ImmunizationRegistry immunizationRegistry) {
        return immunizationIdentifierRepository.findByIdentifierAndImmunizationRegistryId(idType.getIdPart(),immunizationRegistry.getId())
                .orElse(new ImmunizationIdentifier()).getVaccinationEventId();
    }
    public String getImmunizationLocalId(Identifier identifier, Facility facility) {
        if (identifier.getSystem().equals(getFacilityImmunizationIdentifierSystem(facility))) { //
            return identifier.getValue();
        } else {
            return null;
        }
    }

    public String getLocalUrnFromUrn(String urn, ImmunizationRegistry immunizationRegistry, Facility facility) {
        try {
            IdType idType = new IdType(urn);
            if (idType.getResourceType() != null){
                if (idType.getResourceType().equals("Patient")){
                    return "Patient/" + this.getPatientLocalId(idType,immunizationRegistry);
                } else if (idType.getResourceType().equals("Immunization")) {
                    return "Immunization/" + this.getImmunizationLocalId(new IdType(urn), immunizationRegistry);
                }
            }
        } catch (Exception e) {}
        Scanner scanner = new Scanner(urn);
        scanner.useDelimiter("/|#");
        String next = "";
        String prev = "";
        while (scanner.hasNext()) {
            prev = next;
            next = scanner.next();
            if (next.equals("?identifier=")) {
                String identifierFirstPart = scanner.next("\\|");
                Identifier identifier = new Identifier();
                if (scanner.hasNext("\\|")) {
                    identifier.setSystem(identifierFirstPart)
                            .setValue(scanner.next("\\|"));
                } else {
                    identifier.setValue(identifierFirstPart);
                }

                if (prev.equals("Patient")) {
                    return "Patient/" + this.getPatientLocalId(identifier,facility);
                } else if (prev.equals("Immunization")) {
                    return "Immunization/" + this.getImmunizationLocalId(identifier,facility);
                }
            }
        }
        return null;
    }

    public Reference facilityReference(Facility facility) {
        return new Reference().setType("Organization").setIdentifier( new Identifier().setSystem(FACILITY_SYSTEM).setValue(facility.getId()));
    }

    public Identifier facilityIdentifier(Facility facility) {
        return new Identifier().setSystem(FACILITY_SYSTEM).setValue(facility.getId());
    }



//    public void saveNewPatientIdentifier(Reference reference, ImmunizationRegistry immunizationRegistry) {
////        patientIdentifierRepository.save(new PatientIdentifier(patientId,immunizationRegistry.getId(),outcome.getId().getIdPart()));
//
//        return;//TODO ?
//    }
}
