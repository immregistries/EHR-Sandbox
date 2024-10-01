package org.immregistries.ehr.logic;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

import java.util.Map;

/**
 * Interface for Service Writing different kinds of FHIR Transaction Bundles
 */
public interface IFhirTransactionWriter {
    /**
     * Helping method to build Fhir String Urls with Identifier parameter
     *
     * @param base          Url base to extend with parameter
     * @param ehrIdentifier identifier Parameter
     * @return Complete Url with Identifier Parameter
     */
    static String identifierUrl(String base, EhrIdentifier ehrIdentifier) {
        if (ehrIdentifier != null) {
            base += "?identifier=";
            if (StringUtils.isNotBlank(ehrIdentifier.getSystem())) {
                base += ehrIdentifier.getSystem() + "|";
            }
            base += ehrIdentifier.getValue();
        }
        return base;
    }

    /**
     * Fetches or registers clinician in bundle
     *
     * @param bundle          Bundle
     * @param clinician       Clinician
     * @param clinicianUrlMap Map of already added Clinicians Url Map(clinicianLocalId, entryUrl)
     * @return Clinician BundleEntry Url
     */
    default String addOrGetClinicianEntry(IBaseBundle bundle, Clinician clinician, Map<Integer, String> clinicianUrlMap) {
        String reference;
        if (clinician == null) {
            return null;
        }
        reference = clinicianUrlMap.get(clinician.getId());
        if (reference == null) {
            reference = addClinicianEntry(bundle, clinician);
            clinicianUrlMap.put(clinician.getId(), reference);
        }
        return reference;
    }


    /**
     * Prototype for a Bundle equivalent to HL7v2 QPD
     *
     * @param facility   Facility/ organization writing the message
     * @param ehrPatient Patient
     * @return Transaction bundle
     */
    IBaseBundle qpdBundle(Facility facility, EhrPatient ehrPatient);

    /**
     * Prototype for a Bundle equivalent to HL7v2 VXU with a single vaccination
     *
     * @param facility         Facility/ organization writing the message
     * @param vaccinationEvent Vaccination
     * @return Transaction bundle
     */
    IBaseBundle vxuBundle(Facility facility, VaccinationEvent vaccinationEvent);

    /**
     * Bundle transaction for quick testing, including every patient related to a facility, and their iz history, and clinicians
     *
     * @param facility Facility/Organization
     * @return Transaction bundle
     */
    IBaseBundle transactionBundle(Facility facility);

    String addOrganizationEntry(IBaseBundle bundle, Facility facility);

    String addPatientEntry(IBaseBundle bundle, String organizationUrl, EhrPatient ehrPatient, Map<Integer, String> clinicianUrlMap);

    String addVaccinationEntry(IBaseBundle bundle, String patientUrl, VaccinationEvent vaccinationEvent, Map<Integer, String> clinicianUrlMap);

    String addClinicianEntry(IBaseBundle bundle, Clinician clinician);
}
