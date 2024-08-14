package org.immregistries.ehr.logic;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.ImmunizationIdentifierRepository;
import org.immregistries.ehr.api.repositories.PatientIdentifierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Scanner;

import static org.immregistries.ehr.logic.mapping.IPatientMapper.MRN_TYPE_SYSTEM;
import static org.immregistries.ehr.logic.mapping.IPatientMapper.MRN_TYPE_VALUE;

@Service
public class ResourceIdentificationService {
    Logger logger = LoggerFactory.getLogger(ResourceIdentificationService.class);
    public static final String FACILITY_SYSTEM = "ehr-sandbox/facility";
    private static final String IDENTIFIER_SYSTEM_PREFIX = FACILITY_SYSTEM + "/";
    private static final String PATIENT_IDENTIFIER_SYSTEM_SUFFIX = "/patient-system";
    private static final String IMMUNIZATION_IDENTIFIER_SYSTEM_SUFFIX = "/immunization-system";

    /**
     * Defines a system for FHIR identifier system in the facility
     *
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


    public String getLocalPatientId(Patient remotePatient, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String id;
        /**
         * first we check if the patient has known identifier for the facility system or MRN
         */
        for (Identifier identifier : remotePatient.getIdentifier()) {
            id = getLocalPatientId(identifier, facility);
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        /**
         * if not we check if ID is known is external identifier registry
         */
        return getLocalPatientId(new IdType(remotePatient.getId()), immunizationRegistry);
    }

    public String getLocalPatientId(org.hl7.fhir.r4.model.Patient remotePatient, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String id;
        /**
         * first we check if the patient has known identifier for the facility system or MRN
         */
        for (org.hl7.fhir.r4.model.Identifier identifier : remotePatient.getIdentifier()) {
            id = getLocalPatientId(identifier, facility);
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        /**
         * if not we check if ID is known is external identifier registry
         */
        return getLocalPatientId(new IdType(remotePatient.getId()), immunizationRegistry);
    }

    public String getLocalPatientId(Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String localId = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            localId = getLocalPatientId(reference.getIdentifier(), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
        }
        return localId;
    }

    public String getLocalPatientId(org.hl7.fhir.r4.model.Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String localId = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            localId = getLocalPatientId(reference.getIdentifier(), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
        }
        return localId;
    }

    public EhrPatient getLocalPatient(Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        EhrPatient ehrPatient = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            return getLocalPatient(reference.getIdentifier(), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            String localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
            ehrPatient = ehrPatientRepository.findById(localId).orElse(null);
        }
        return ehrPatient;
    }

    public EhrPatient getLocalPatient(org.hl7.fhir.r4.model.Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        EhrPatient ehrPatient = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            return getLocalPatient(reference.getIdentifier(), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            String localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
            ehrPatient = ehrPatientRepository.findById(localId).orElse(null);
        }
        return ehrPatient;
    }

    public String getLocalPatientId(IdType idType, ImmunizationRegistry immunizationRegistry) {
        return patientIdentifierRepository.findByIdentifierAndImmunizationRegistryId(idType.getIdPart(), immunizationRegistry.getId())
                .orElse(new PatientExternalIdentifier()).getPatientId();
    }

    public EhrPatient getLocalPatient(Identifier identifier, Facility facility) {
        EhrPatient ehrPatient = null;
        if (identifier.hasType() && identifier.getType().getCoding().stream().anyMatch(coding -> coding.getCode().equals(MRN_TYPE_VALUE) && coding.getSystem().equals(MRN_TYPE_SYSTEM))) {
            ehrPatient = ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), identifier.getValue()).orElse(null);
        }
        if (ehrPatient == null) {
            ehrPatient = ehrPatientRepository.findByFacilityIdAndIdentifier(facility.getId(), StringUtils.defaultIfBlank(identifier.getSystem(), ""), identifier.getValue())
                    .orElse(null);
        }
        return ehrPatient;
    }

    public EhrPatient getLocalPatient(org.hl7.fhir.r4.model.Identifier identifier, Facility facility) {
        EhrPatient ehrPatient = null;
        if (identifier.hasType() && identifier.getType().getCoding().stream().anyMatch(coding -> coding.getCode().equals(MRN_TYPE_VALUE) && coding.getSystem().equals(MRN_TYPE_SYSTEM))) {
            ehrPatient = ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), identifier.getValue()).orElse(null);
        }
        if (ehrPatient == null) {
            ehrPatient = ehrPatientRepository.findByFacilityIdAndIdentifier(facility.getId(), StringUtils.defaultIfBlank(identifier.getSystem(), ""), identifier.getValue())
                    .orElse(null);
        }
        return ehrPatient;
    }

    public String getLocalPatientId(Identifier identifier, Facility facility) {
        EhrPatient ehrPatient = getLocalPatient(identifier, facility);
        if (ehrPatient == null) {
            return null;
        }
        return ehrPatient.getId();
    }

    public String getLocalPatientId(org.hl7.fhir.r4.model.Identifier identifier, Facility facility) {
        EhrPatient ehrPatient = getLocalPatient(identifier, facility);
        if (ehrPatient == null) {
            return null;
        }
        return ehrPatient.getId();
    }

    public String getImmunizationLocalId(Immunization remoteImmunization, ImmunizationRegistry immunizationRegistry, Facility facility) {
        String id = "";
        /**
         * first we check if the patient has known identifier for the facility system
         */
        for (Identifier identifier : remoteImmunization.getIdentifier()) {
            id = getImmunizationLocalId(identifier, facility);
            if (id != null && !id.isBlank()) {
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
        } else if (reference.getIdentifier() != null) {
            return getImmunizationLocalId(reference.getIdentifier(), facility);
        } else {
            return null;
        }
    }

    public String getImmunizationLocalId(IdType idType, ImmunizationRegistry immunizationRegistry) {
        return immunizationIdentifierRepository.findByIdentifierAndImmunizationRegistryId(idType.getIdPart(), immunizationRegistry.getId())
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
            if (idType.getResourceType() != null) {
                if (idType.getResourceType().equals("Patient")) {
                    return "Patient/" + this.getLocalPatientId(idType, immunizationRegistry);
                } else if (idType.getResourceType().equals("Immunization")) {
                    return "Immunization/" + this.getImmunizationLocalId(new IdType(urn), immunizationRegistry);
                }
            }
        } catch (Exception e) {
        }
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
                    return "Patient/" + this.getLocalPatientId(identifier, facility);
                } else if (prev.equals("Immunization")) {
                    return "Immunization/" + this.getImmunizationLocalId(identifier, facility);
                }
            }
        }
        return null;
    }

    public Reference facilityReferenceR5(Facility facility) {
        return new Reference().setType("Organization").setIdentifier(facilityIdentifierR5(facility));
    }

    public org.hl7.fhir.r4.model.Reference facilityReferenceR4(Facility facility) {
        return new org.hl7.fhir.r4.model.Reference().setType("Organization").setIdentifier(facilityIdentifierR4(facility));
    }

    public Identifier facilityIdentifierR5(Facility facility) {
        if (facility.getIdentifiers().isEmpty()) {
            return new Identifier().setSystem(FACILITY_SYSTEM).setValue(facility.getId());
        } else {
            return facility.getIdentifiers().stream().findFirst().get().toR5();
        }
    }

    public org.hl7.fhir.r4.model.Identifier facilityIdentifierR4(Facility facility) {
        if (facility.getIdentifiers().isEmpty()) {
            return new org.hl7.fhir.r4.model.Identifier().setSystem(FACILITY_SYSTEM).setValue(facility.getId());
        } else {
            return facility.getIdentifiers().stream().findFirst().get().toR4();
        }
    }
}
