package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

public interface IPatientMapper<Patient extends IBaseResource> extends IEhrEntityFhirMapper<EhrPatient> {
    String MRN_SYSTEM = "mrn";
    String MRN_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0203";
    String MRN_TYPE_VALUE = "MR";
    String MOTHER_MAIDEN_NAME = "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName";
    String REGISTRY_STATUS_EXTENSION = "registryStatus";
    String REGISTRY_STATUS_INDICATOR = "registryStatusIndicator";
    String ETHNICITY_EXTENSION = "ethnicity";
    String ETHNICITY_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-Ethnicity";
    String RACE = "race";
    String RACE_SYSTEM = "https://terminology.hl7.org/2.0.0/CodeSystem-v3-Race.html";
    String PUBLICITY_EXTENSION = "publicity";
    String PUBLICITY_SYSTEM = "publicityIndicator";
    String PROTECTION_EXTENSION = "protection";
    String PROTECTION_SYSTEM = "protectionIndicator";
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
