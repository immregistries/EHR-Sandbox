package org.immregistries.ehr.shlink.service;

import com.google.gson.JsonObject;
import io.jsonwebtoken.CompressionException;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.logic.BundleImportServiceR4;
import org.immregistries.ehr.logic.BundleImportServiceR5;
import org.immregistries.ehr.logic.IBundleImportService;
import org.immregistries.ehr.logic.mapping.MappingHelper;
import org.immregistries.ehr.logic.mapping.forR4.ImmunizationMapperR4;
import org.immregistries.ehr.logic.mapping.forR4.PatientMapperR4;
import org.immregistries.ehr.logic.mapping.forR5.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.forR5.PatientMapperR5;
import org.immregistries.ehr.logic.mapping.interfaces.IImmunizationMapper;
import org.immregistries.ehr.logic.mapping.interfaces.IPatientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.immregistries.ehr.shlink.SmartHealthConstants.*;

@Service
public class VerifiableCredentialBundleExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BundleImportServiceR5 bundleImportServiceR5;
    @Autowired
    private BundleImportServiceR4 bundleImportServiceR4;
    @Autowired
    private ImmunizationMapperR5 immunizationMapperR5;
    @Autowired
    private ImmunizationMapperR4 immunizationMapperR4;
    @Autowired
    private PatientMapperR5 patientMapperR5;
    @Autowired
    private PatientMapperR4 patientMapperR4;

    public List<VaccinationEvent> parseBundleVaccinationsFromVC(JsonObject vc) throws CompressionException {
        if (vc.has(CREDENTIAL_SUBJECT)) {
            JsonObject credentialSubject = vc.getAsJsonObject(CREDENTIAL_SUBJECT);
            String fhirVersion = credentialSubject.get(FHIR_VERSION).getAsString();
            String fhirBundle = credentialSubject.get(FHIR_BUNDLE).toString();
            IBundleImportService iBundleImportService;
            IImmunizationMapper immunizationMapper;
            if (fhirVersion.startsWith("4.0")) {
                iBundleImportService = bundleImportServiceR4;
                immunizationMapper = immunizationMapperR4;
            } else if (fhirVersion.startsWith("5.")) {
                iBundleImportService = bundleImportServiceR5;
                immunizationMapper = immunizationMapperR5;
            } else {
                throw new RuntimeException("FHIR Version not supported " + fhirVersion);
            }
            return extractVaccinationEvents(fhirBundle, iBundleImportService, immunizationMapper);
        }
        return null;
    }


    public EhrPatient parseBundlePatientFromVC(JsonObject vc) throws CompressionException {
        if (vc.has(CREDENTIAL_SUBJECT)) {
            JsonObject credentialSubject = vc.getAsJsonObject(CREDENTIAL_SUBJECT);
            String fhirVersion = credentialSubject.get(FHIR_VERSION).getAsString();
            String fhirBundle = credentialSubject.get(FHIR_BUNDLE).toString();
            IBundleImportService iBundleImportService;
            IPatientMapper patientMapper;
            if (fhirVersion.startsWith("4.0")) {
                iBundleImportService = bundleImportServiceR4;
                patientMapper = patientMapperR4;
            } else if (fhirVersion.startsWith("5.")) {
                iBundleImportService = bundleImportServiceR5;
                patientMapper = patientMapperR5;
            } else {
                throw new RuntimeException("FHIR Version not supported " + fhirVersion);
            }
            return extractPatient(fhirBundle, iBundleImportService, patientMapper);
        }
        return null;
    }

    public List<VaccinationEvent> parseBundleVaccinationsUnknownVersion(String fhirBundle) {
        IBundleImportService iBundleImportService;
        IImmunizationMapper immunizationMapper;
        try {
            iBundleImportService = bundleImportServiceR4;
            immunizationMapper = immunizationMapperR4;
            return extractVaccinationEvents(fhirBundle, iBundleImportService, immunizationMapper);
        } catch (Exception e) {
            e.printStackTrace();
            iBundleImportService = bundleImportServiceR5;
            immunizationMapper = immunizationMapperR5;
            return extractVaccinationEvents(fhirBundle, iBundleImportService, immunizationMapper);
        }
    }


    private List<VaccinationEvent> extractVaccinationEvents(String fhirBundle, IBundleImportService iBundleImportService, IImmunizationMapper immunizationMapper) {
        List<IDomainResource> list = iBundleImportService.domainResourcesFromBaseBundleEntries(fhirBundle);
        List<VaccinationEvent> vaccinationEvents = new ArrayList<>(5);
        for (IDomainResource iDomainResource : list) {
            if (iDomainResource.fhirType().equals(MappingHelper.IMMUNIZATION)) {
                VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(iDomainResource);
                vaccinationEvents.add(vaccinationEvent);
            }
        }
        return vaccinationEvents;
    }

    private EhrPatient extractPatient(String fhirBundle, IBundleImportService iBundleImportService, IPatientMapper patientMapper) {
        List<IDomainResource> list = iBundleImportService.domainResourcesFromBaseBundleEntries(fhirBundle);
        for (IDomainResource iDomainResource : list) {
            if (iDomainResource.fhirType().equals(MappingHelper.PATIENT)) {
                return patientMapper.toEhrPatient(iDomainResource);
            }
        }
        return null;
    }

}
