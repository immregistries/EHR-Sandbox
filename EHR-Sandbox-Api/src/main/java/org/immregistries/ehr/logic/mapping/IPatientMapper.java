package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

public interface IPatientMapper<Patient extends IBaseResource> extends IEhrEntityFhirMapper<EhrPatient> {
    String MRN_SYSTEM = "mrn";
    String MRN_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0203";
    String MRN_TYPE_VALUE = "MR";
    String MOTHER_MAIDEN_NAME_EXTENSION = "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName";
    String REGISTRY_STATUS_EXTENSION = "registryStatus";
    String REGISTRY_STATUS_INDICATOR = "http://terminology.hl7.org/ValueSet/v2-0441";
    String ETHNICITY_EXTENSION = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity";
    String ETHNICITY_EXTENSION_DETAILED = "detailed";
    String ETHNICITY_EXTENSION_OMB = "ombCategory";
    String ETHNICITY_EXTENSION_TEXT = "text";
    String ETHNICITY_SYSTEM = "urn:oid:2.16.840.1.113883.6.238";
    String RACE_EXTENSION = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race";
    String RACE_EXTENSION_DETAILED = "detailed";
    String RACE_EXTENSION_OMB = "ombCategory";
    String RACE_EXTENSION_TEXT = "text";
    String RACE_SYSTEM = "urn:oid:2.16.840.1.113883.6.238";
    String RACE_SYSTEM_OMB = "http://hl7.org/fhir/us/core/ValueSet/omb-ethnicity-category";
    String RACE_SYSTEM_DETAILED = "http://hl7.org/fhir/us/core/ValueSet/detailed-race";

    String PUBLICITY_EXTENSION = "publicity";
    String PUBLICITY_SYSTEM = "http://terminology.hl7.org/ValueSet/v2-0215";
    String PROTECTION_EXTENSION = "protection";
    String PROTECTION_SYSTEM = "http://terminology.hl7.org/ValueSet/v2-0136";
    String YES = "Y";
    String NO = "N";


    Patient toFhir(EhrPatient ehrPatient, Facility facility);

    Patient toFhir(EhrPatient ehrPatient);

    /**
     * Used to extract identifier for Request Parameter when resource comes from parsing another request
     *
     * @param patient
     * @return
     */
    EhrIdentifier getPatientIdentifier(IBaseResource patient);

    EhrPatient toEhrPatient(Patient patient);

}
