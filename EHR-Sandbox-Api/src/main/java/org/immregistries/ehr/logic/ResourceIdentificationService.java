package org.immregistries.ehr.logic;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.IdType;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.ImmunizationIdentifierRepository;
import org.immregistries.ehr.api.repositories.PatientIdentifierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Scanner;

import static org.immregistries.ehr.logic.mapping.IPatientMapper.MRN_TYPE_VALUE;

@Service
public class ResourceIdentificationService {
    Logger logger = LoggerFactory.getLogger(ResourceIdentificationService.class);
    public static final String FACILITY_SYSTEM = "ehr-sandbox/facility";
    public static final String CLINICIAN_SYSTEM = "ehr-sandbox/clinician";
    private static final String IDENTIFIER_SYSTEM_PREFIX = FACILITY_SYSTEM + "/";
    private static final String PATIENT_IDENTIFIER_SYSTEM_SUFFIX = "/patient-system";
    private static final String IMMUNIZATION_IDENTIFIER_SYSTEM_SUFFIX = "/immunization-system";
    private static final String GROUP_IDENTIFIER_SYSTEM_SUFFIX = "/group-system";

    /**
     * Defines a system for FHIR identifier system in the facility
     * TODO check if parent Facility
     *
     * @param facility Facility to define the system for
     * @return
     */
    public String getFacilityPatientIdentifierSystem(Facility facility) {
        if (facility.getParentFacility() != null) {
            return getFacilityPatientIdentifierSystem(facility.getParentFacility());
        } else {
            return IDENTIFIER_SYSTEM_PREFIX + facility.getId() + PATIENT_IDENTIFIER_SYSTEM_SUFFIX;
        }
    }

    public String getFacilityImmunizationIdentifierSystem(Facility facility) {
        if (facility.getParentFacility() != null) {
            return getFacilityImmunizationIdentifierSystem(facility.getParentFacility());
        }
        return IDENTIFIER_SYSTEM_PREFIX + facility.getId() + IMMUNIZATION_IDENTIFIER_SYSTEM_SUFFIX;
    }

    public String getFacilityGroupIdentifierSystem(Facility facility) {
        if (facility.getParentFacility() != null) {
            return getFacilityGroupIdentifierSystem(facility.getParentFacility());
        }
        return IDENTIFIER_SYSTEM_PREFIX + facility.getId() + GROUP_IDENTIFIER_SYSTEM_SUFFIX;
    }

    @Autowired
    PatientIdentifierRepository patientIdentifierRepository;
    @Autowired
    ImmunizationIdentifierRepository immunizationIdentifierRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;


    public Integer getLocalPatientId(org.hl7.fhir.r5.model.Patient remotePatient, ImmunizationRegistry immunizationRegistry, Facility facility) {
        Integer id;
        /**
         * first we check if the patient has known identifier for the facility system or MRN
         */
        for (org.hl7.fhir.r5.model.Identifier identifier : remotePatient.getIdentifier()) {
            id = getLocalPatientId(new EhrIdentifier(identifier), facility);
            if (id != null && id > 0) {
                return id;
            }
        }
        /**
         * if not we check if ID is known is external identifier registry
         */
        return getLocalPatientId(new IdType(remotePatient.getId()), immunizationRegistry);
    }

    public Integer getLocalPatientId(org.hl7.fhir.r4.model.Patient remotePatient, ImmunizationRegistry immunizationRegistry, Facility facility) {
        Integer id;
        /**
         * first we check if the patient has known identifier for the facility system or MRN
         */
        for (org.hl7.fhir.r4.model.Identifier identifier : remotePatient.getIdentifier()) {
            id = getLocalPatientId(new EhrIdentifier(identifier), facility);
            if (id != null && id > 0) {
                return id;
            }
        }
        /**
         * if not we check if ID is known is external identifier registry
         */
        return getLocalPatientId(new IdType(remotePatient.getId()), immunizationRegistry);
    }

    public Integer getLocalPatientId(org.hl7.fhir.r5.model.Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        Integer localId = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            localId = getLocalPatientId(new EhrIdentifier(reference.getIdentifier()), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
        }
        return localId;
    }

    public Integer getLocalPatientId(org.hl7.fhir.r4.model.Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        Integer localId = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            localId = getLocalPatientId(new EhrIdentifier(reference.getIdentifier()), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
        }
        return localId;
    }

    public EhrPatient getLocalPatient(org.hl7.fhir.r5.model.Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        EhrPatient ehrPatient = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            return getLocalPatient(new EhrIdentifier(reference.getIdentifier()), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            Integer localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
            ehrPatient = ehrPatientRepository.findById(localId).orElse(null);
        }
        return ehrPatient;
    }

    public EhrPatient getLocalPatient(org.hl7.fhir.r4.model.Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        EhrPatient ehrPatient = null;
        if (reference.getIdentifier() != null && StringUtils.isNotBlank(reference.getIdentifier().getValue())) {
            return getLocalPatient(new EhrIdentifier(reference.getIdentifier()), facility);
        } else if (reference.getReference() != null && !reference.getReference().isBlank() && immunizationRegistry != null) {
            Integer localId = getLocalPatientId(new IdType(reference.getReference()), immunizationRegistry);
            ehrPatient = ehrPatientRepository.findById(localId).orElse(null);
        }
        return ehrPatient;
    }

    public Integer getLocalPatientId(IdType idType, ImmunizationRegistry immunizationRegistry) {
        return patientIdentifierRepository.findByIdentifierAndImmunizationRegistryId(idType.getIdPart(), immunizationRegistry.getId())
                .orElse(new PatientExternalIdentifier()).getPatientId();
    }

    public EhrPatient getLocalPatient(EhrIdentifier ehrIdentifier, Facility facility) {
        EhrPatient ehrPatient = null;
        if (ehrIdentifier.getType() != null && ehrIdentifier.getType().equals(MRN_TYPE_VALUE)) {
            ehrPatient = ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), ehrIdentifier.getValue()).orElse(null);
        }
        if (ehrPatient == null) {
            ehrPatient = ehrPatientRepository.findByFacilityIdAndIdentifier(facility.getId(), StringUtils.defaultIfBlank(ehrIdentifier.getSystem(), ""), ehrIdentifier.getValue())
                    .orElse(null);
        }
        return ehrPatient;
    }

    public Integer getLocalPatientId(EhrIdentifier ehrIdentifier, Facility facility) {
        EhrPatient ehrPatient = getLocalPatient(ehrIdentifier, facility);
        if (ehrPatient == null) {
            return null;
        }
        return ehrPatient.getId();
    }

    public Integer getImmunizationLocalId(org.hl7.fhir.r5.model.Immunization remoteImmunization, ImmunizationRegistry immunizationRegistry, Facility facility) {
        Integer id = null;
        /**
         * first we check if the patient has known identifier for the facility system
         */
        for (org.hl7.fhir.r5.model.Identifier identifier : remoteImmunization.getIdentifier()) {
            id = getImmunizationLocalId(new EhrIdentifier(identifier), facility);
            if (id != null && id > 0) {
                return id;
            }
        }
        /**
         * if not we check if id is known is external identifier registry
         */
        return getImmunizationLocalId(new IdType(remoteImmunization.getId()), immunizationRegistry);
    }

    public Integer getImmunizationLocalId(org.hl7.fhir.r4.model.Immunization remoteImmunization, ImmunizationRegistry immunizationRegistry, Facility facility) {
        Integer id;
        /**
         * first we check if the patient has known identifier for the facility system
         */
        for (org.hl7.fhir.r4.model.Identifier identifier : remoteImmunization.getIdentifier()) {
            id = getImmunizationLocalId(new EhrIdentifier(identifier), facility);
            if (id != null && id > 0) {
                return id;
            }
        }
        /**
         * if not we check if id is known is external identifier registry
         */
        return getImmunizationLocalId(new IdType(remoteImmunization.getId()), immunizationRegistry);
    }


    public Integer getImmunizationLocalId(org.hl7.fhir.r5.model.Reference reference, ImmunizationRegistry immunizationRegistry, Facility facility) {
        if (reference.getReference() != null && !reference.getReference().isBlank()) {
            return getImmunizationLocalId(new IdType(reference.getReference()), immunizationRegistry);
        } else if (reference.getIdentifier() != null) {
            return getImmunizationLocalId(new EhrIdentifier(reference.getIdentifier()), facility);
        } else {
            return null;
        }
    }

    public Integer getImmunizationLocalId(IdType idType, ImmunizationRegistry immunizationRegistry) {
        return immunizationIdentifierRepository.findByIdentifierAndImmunizationRegistryId(idType.getIdPart(), immunizationRegistry.getId())
                .orElse(new ImmunizationIdentifier()).getVaccinationEventId();
    }

    public Integer getImmunizationLocalId(EhrIdentifier ehrIdentifier, Facility facility) {
        if (ehrIdentifier.getSystem().equals(getFacilityImmunizationIdentifierSystem(facility))) {
            try {
                return EhrUtils.convert(ehrIdentifier.getValue());
            } catch (NumberFormatException numberFormatException) {
                return null;
            }
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
                EhrIdentifier ehrIdentifier = new EhrIdentifier();
                if (scanner.hasNext("\\|")) {
                    ehrIdentifier.setSystem(identifierFirstPart);
                    ehrIdentifier.setValue(scanner.next("\\|"));
                } else {
                    ehrIdentifier.setValue(identifierFirstPart);
                }

                if (prev.equals("Patient")) {
                    return "Patient/" + this.getLocalPatientId(ehrIdentifier, facility);
                } else if (prev.equals("Immunization")) {
                    return "Immunization/" + this.getImmunizationLocalId(ehrIdentifier, facility);
                }
            }
        }
        return null;
    }

}
