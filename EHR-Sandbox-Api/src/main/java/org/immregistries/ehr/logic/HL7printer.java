package org.immregistries.ehr.logic;


import org.apache.commons.lang3.StringUtils;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.*;
import org.immregistries.ehr.logic.mapping.IOrganizationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

@Service
public class HL7printer {

    Logger logger = LoggerFactory.getLogger(HL7printer.class);

    @Autowired
    CodeMapManager codeMapManager;
    private SimpleDateFormat simpleDateFormat;

    private String formatDate(Date date) {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        }
        if (date == null) {
            return "";
        } else {
            return simpleDateFormat.format(date);
        }

    }

    public String buildVxu(VaccinationEvent vaccinationEvent, EhrPatient patient, Facility facility) {
        StringBuilder sb = new StringBuilder();
        CodeMap codeMap = codeMapManager.getCodeMap();
        createMSH(sb, "VXU^V04^VXU_V04", "Z22", facility);
        printQueryPID(sb, patient, 1);
        printPD1(sb, patient);
        printQueryNK1(sb, patient);

        int obxSetId = 0;
        int obsSubId = 0;
        if (vaccinationEvent != null) {
            Vaccine vaccine = vaccinationEvent.getVaccine();
//            Code cvxCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_CVX_CODE, vaccine.getVaccineCvxCode());
//            if (cvxCode != null)) { // TODO maybe remove condition or set with Flavor
            sb.append("\n");
            printORC(sb, facility, vaccinationEvent);
            sb.append("RXA");
            // RXA-1
            sb.append("|0");
            // RXA-2
            sb.append("|1");
            // RXA-3
            sb.append("|").append(formatDate(vaccine.getAdministeredDate()));
            // RXA-4
            sb.append("|");
            // RXA-5
            sb.append("|");
            printCode(sb, vaccine.getVaccineCvxCode(), CodesetType.VACCINATION_CVX_CODE, "CVX");
            if (StringUtils.isNotBlank(vaccine.getVaccineNdcCode())) {
                Code ndcCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_NDC_CODE, vaccine.getVaccineNdcCode());
                if (ndcCode != null) {
                    sb.append("~");
                    printCode(sb, ndcCode, "NDC");
                }
            }
            {
                // RXA-6
                sb.append("|");
                sb.append(StringUtils.defaultIfBlank(vaccine.getAdministeredAmount(), ""));
                // RXA-7
                sb.append("|");
                if (StringUtils.isNotBlank(vaccine.getInformationStatement())) {
                    sb.append("mL^milliliters^UCUM");
                }
            }
            // RXA-8
            sb.append("|");
            // RXA-9
            sb.append("|");
            {
                Code informationCode = null;
                if (vaccine.getInformationSource() != null) {
                    informationCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_INFORMATION_SOURCE, vaccine.getInformationSource());
                }
                if (informationCode != null) {
                    printCode(sb, informationCode, "NIP001");
                }
            }
            // RXA-10
            sb.append("|");
            printXCN(sb, vaccinationEvent.getAdministeringClinician());
            // RXA-11
            sb.append("|");
            sb.append("^^^");
            // RXA-12
            sb.append("|");
            // RXA-13
            sb.append("|");
            // RXA-14
            sb.append("|");
            // RXA-15
            sb.append("|");
            if (vaccine.getLotNumber() != null) {
                sb.append(vaccine.getLotNumber());
            }
            // RXA-16
            sb.append("|");
            if (vaccine.getExpirationDate() != null) {
                sb.append(formatDate(vaccine.getExpirationDate()));
            }
            // RXA-17
            sb.append("|");
            printCode(sb, vaccine.getVaccineMvxCode(), CodesetType.VACCINATION_MANUFACTURER_CODE, "MVX");
            // RXA-18
            sb.append("|");
            printCode(sb, vaccine.getRefusalReasonCode(), CodesetType.VACCINATION_REFUSAL, "NIP002");
            // RXA-19
            sb.append("|");
            // RXA-20
            sb.append("|");
            String completionStatus = vaccine.getCompletionStatus();
            if (StringUtils.isBlank(completionStatus)) {
                completionStatus = "CP";
            }
            printCode(sb, completionStatus, CodesetType.VACCINATION_COMPLETION, null);

            // RXA-21
            String actionCode = vaccine.getActionCode();
            sb.append("|");
            if (StringUtils.isBlank(actionCode)) {
                actionCode = "A";
            }
            sb.append(actionCode);
            sb.append("\r");
            if (StringUtils.isNotBlank(vaccine.getBodyRoute())) {
                sb.append("RXR");
                // RXR-1
                sb.append("|");
                printCode(sb, vaccine.getBodyRoute(), CodesetType.BODY_ROUTE, "NCIT");
                // RXR-2
                sb.append("|");
                printCode(sb, vaccine.getBodySite(), CodesetType.BODY_SITE, "HL70163");
                sb.append("\r");
            }

            codeMap.getCodesForTable(CodesetType.OBSERVATION_IDENTIFIER);

            obsSubId++;
            {
                obxSetId++;
                String loinc = "64994-7";
                String loincLabel = "Eligibility Status";
                String valueTable = "HL70064";
                String value;
                String method;
                if (StringUtils.isNotBlank(vaccine.getFinancialStatus())) {
                    value = vaccine.getFinancialStatus();
                    method = "VXC40^Eligibility captured at the immunization level^CDCPHINVS";
                } else {
                    value = patient.getFinancialStatus();
                    method = "VXC41^Eligibility captured at the visit level^CDCPHINVS";
                }
                printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, value, CodesetType.FINANCIAL_STATUS_CODE, valueTable, method);
            }

            if (StringUtils.isNotBlank(vaccine.getFundingSource())) {
                obxSetId++;
                obsSubId++;
                String loinc = "30963-3";
                String loincLabel = "Vaccine funding source";
                String valueTable = "CDCPHINVS";
                String value = vaccine.getFundingSource();
                printObx(sb, obxSetId, obsSubId, null, loinc, loincLabel, value, CodesetType.FINANCIAL_STATUS_CODE, valueTable, "");
            }

            obsSubId++;
            // page 24 https://www.cdc.gov/vaccines/programs/iis/technical-guidance/downloads/hl7-clarification-r6.pdf
            if (StringUtils.isNotBlank(vaccine.getInformationStatement())) {
                obxSetId++;
                String loinc = "69764-9";
                String loincLabel = "Document Type";
                String value = vaccine.getInformationStatement();
                String valueTable = "cdcgs1vis";
                printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, value, CodesetType.VACCINATION_VIS_DOC_TYPE, valueTable, "");
            } else {
                if (StringUtils.isNotBlank(vaccine.getInformationStatementCvx())) {
                    obxSetId++;
                    String loinc = "30956-7";
                    String loincLabel = "Vaccine type";
                    String value = vaccine.getInformationStatementCvx();
                    String valueTable = "CVX";
                    printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, value, CodesetType.VACCINATION_CVX_CODE, valueTable, "");
                }
            }
            if (vaccine.getInformationStatementPublishedDate() != null) {
                obxSetId++;
                String loinc = "29768-9";
                String loincLabel = "Date Vaccine Information Statement Published";
                Date date = vaccine.getInformationStatementPublishedDate();
                printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, date);
            }

            if (vaccine.getInformationStatementPresentedDate() != null) {
                obxSetId++;
                String loinc = "29769-7";
                String loincLabel = "Date Vaccine Information Statement Presented";
                Date date = vaccine.getInformationStatementPresentedDate();
                printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, date);
            }
//            }
        }
        return sb.toString();
    }

    public void printQueryNK1(StringBuilder sb, EhrPatient patient) {
        // https://hl7-definition.caristix.com/v2/HL7v2.5/Segments/NK1
        int count = 0;
        for (NextOfKinRelationship nextOfKinRelationship : patient.getNextOfKinRelationships()) {
            NextOfKin nextOfKin = nextOfKinRelationship.getNextOfKin();
            if (nextOfKin != null) {
                count++;
                sb.append("\r");
                sb.append("NK1");
//                NK1-1
                sb.append("|").append(count);
//                NK1-2
                sb.append("|")
                        .append(StringUtils.defaultIfBlank(nextOfKin.getNameLast(), ""))
                        .append("^")
                        .append(StringUtils.defaultIfBlank(nextOfKin.getNameFirst(), ""))
                        .append("^")
                        .append(StringUtils.defaultIfBlank(nextOfKin.getNameMiddle(), ""))
                        .append("^")
                        .append(StringUtils.defaultIfBlank(nextOfKin.getNameSuffix(), ""))
                        .append("^^^L");
//                NK1-3
                sb.append("|");
                printCode(sb, nextOfKinRelationship.getRelationshipKind(), CodesetType.PERSON_RELATIONSHIP, "HL70063");
//                NK1-4
//                Optional<EhrAddress> ehrAddress = nextOfKin.getAddresses().stream().findFirst();
                sb.append("|");
                nextOfKin.getAddresses().stream().findFirst().ifPresent(address -> printXAD(sb, address));
//                NK1-5
                sb.append("|");
                Optional<EhrPhoneNumber> ehrPhoneNumber = nextOfKin.getPhoneNumbers().stream().findFirst();
                if (ehrPhoneNumber.isPresent()) {
                    printXTN(sb, ehrPhoneNumber.get());
                    if (StringUtils.isNotBlank(nextOfKin.getEmail())) {
                        sb.append("~");
                    }
                }
//                NK1-5[2]
                if (StringUtils.isNotBlank(nextOfKin.getEmail())) {
                    printXtnEmail(sb, nextOfKin.getEmail());
                }
            }
        }
    }

    public void printQueryPID(StringBuilder sb, EhrPatient patient, int pidCount) {
        // PID
        sb.append("PID");
        // PID-1
        sb.append("|").append(pidCount);
        // PID-2
        sb.append("|");
        // PID-3
        EhrIdentifier mrn = patient.getMrnEhrIdentifier();
        if (mrn != null) {
            sb.append("|").append(mrn.getValue()).append("^^^").append(StringUtils.defaultIfBlank(mrn.getSystem(), "")).append("^MR");
        } else {
            sb.append("|").append(patient.getId()).append("^^^EHR^MR");
        }
        // PID-4
        sb.append("|");
        // PID-5

        sb.append("|");
        printName(sb, patient.getNameFirst(), patient.getNameMiddle(), patient.getNameLast());

        // PID-6
        sb.append("|").append(patient.getMotherMaiden() == null ? "" : patient.getMotherMaiden()).append("^^^^^^M");
        // PID-7
        String dateOfBirth = patient.getBirthDate() == null ? "" : formatDate(patient.getBirthDate());
        sb.append("|").append(dateOfBirth);
        // PID-8
        {
            String sex = patient.getSex();
            if (!sex.equals("F") && !sex.equals("M") && !sex.equals("X")) {
                sex = "U";
            }
            sb.append("|").append(sex);
        }
        // PID-9
        sb.append("|");
        // PID-10
        sb.append("|");
        {
            Iterator<EhrRace> iterator = patient.getRaces().iterator();
            if (iterator.hasNext()) {
                printCode(sb, iterator.next().getValue(), CodesetType.PATIENT_RACE, "0005");
            }
            while (iterator.hasNext()) {
                sb.append("~");
                printCode(sb, iterator.next().getValue(), CodesetType.PATIENT_RACE, "0005");
            }
        }
        // PID-11
        sb.append("|");
        {
            Iterator<EhrAddress> iterator = patient.getAddresses().iterator();
            if (iterator.hasNext()) {
                printXAD(sb, iterator.next());
            }
            while (iterator.hasNext()) {
                sb.append("~");
                printXAD(sb, iterator.next());
            }
        }
        // PID-12
        sb.append("|");
        // PID-13
        sb.append("|");
        {
            Iterator<EhrPhoneNumber> iterator = patient.getPhones().iterator();
            if (iterator.hasNext()) {
                printXTN(sb, iterator.next());
            }
            while (iterator.hasNext()) {
                sb.append("~");
                printXTN(sb, iterator.next());
            }
            if (StringUtils.isNotBlank(patient.getEmail())) {
                if (!patient.getPhones().isEmpty()) {
                    sb.append('~');
                }
                printXtnEmail(sb, patient.getEmail());
            }
        }
        // PID-14
        sb.append("|");
        // PID-15
        sb.append("|");
        // PID-16
        sb.append("|");
        // PID-17
        sb.append("|");
        // PID-18
        sb.append("|");
        // PID-19
        sb.append("|");
        // PID-20
        sb.append("|");
        // PID-21
        sb.append("|");
        // PID-22
        sb.append("|");
        Code ethnicityCode = codeMapManager.getCodeMap().getCodeForCodeset(CodesetType.PATIENT_ETHNICITY, patient.getEthnicity());
        if (ethnicityCode != null) {
            sb.append("^");
            printCode(sb, ethnicityCode, "CDCREC");
        }
        // PID-23
        sb.append("|");
        // PID-24
        sb.append("|");
        sb.append(StringUtils.defaultIfBlank(patient.getBirthFlag(), ""));
        // PID-25
        sb.append("|");
        sb.append(StringUtils.defaultIfBlank(patient.getBirthOrder(), ""));
        sb.append("\r");
    }


    public void createMSH(StringBuilder sb, String messageType, String profileId, Facility facility) {
        // TODO Check that this is accurate as previous implementation did not fit the MSH doc
        String sendingApp = "EHR Sandbox" + " v" + EhrApiApplication.VERSION;
        String sendingFacIdentifier;
        if (Objects.nonNull(facility)) {
            EhrIdentifier ehrIdentifier = IOrganizationMapper.facilityGetOneEhrIdentifier(facility);
            sendingFacIdentifier = StringUtils.defaultIfBlank(ehrIdentifier.getAssignerReference(), "") + "^"
                    + StringUtils.defaultIfBlank(ehrIdentifier.getValue(), "") + "^" +
                    StringUtils.defaultIfBlank(ehrIdentifier.getType(), "");
//        "^" + identifier.getValue() + "^L,M,N";
        } else {
            sendingFacIdentifier = "";
        }
        String receivingApp = "";
        String receivingFac = "";

        String sendingDateString;
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmssZ");
            sendingDateString = simpleDateFormat.format(new Date());
        }
        String uniqueId;
        {
            uniqueId = String.valueOf(System.currentTimeMillis()) + nextIncrement();
        }
        String production = "P";
        // build MSH TODO verify order
//      MSH.1 | MSH.2
        sb.append("MSH|^~\\&|");
//      MSH.3
        sb.append(sendingApp);
        sb.append("|");
//      MSH.4
        sb.append(sendingFacIdentifier);
        sb.append("|");
//      MSH.5
        sb.append(receivingApp);
        sb.append("|");
//      MSH.6
        sb.append(receivingFac);
        sb.append("|");
//      MSH.7
        sb.append(sendingDateString);
        sb.append("|");
//      MSH.8
        sb.append("|");
//      MSH.9
        sb.append(messageType);
        sb.append("|");
        sb.append(uniqueId);
        sb.append("|");
        sb.append(production);
        sb.append("2.5.1|");
        sb.append("|");
        sb.append("|");
        sb.append("ER|");
        sb.append("AL|");
        sb.append("|");
        sb.append("|");
        sb.append("|");
        sb.append("|");
        sb.append(profileId).append("^CDCPHINVS");
        if (Objects.nonNull(facility)) {
            sb.append("|").append(StringUtils.defaultIfBlank(facility.getNameDisplay(), ""));
        }
        sb.append("\r");
    }


    public void printObx(StringBuilder sb, int obxSetId, int obsSubId, Date observationDate, String loinc,
                         String loincLabel, Date date) {
        if (date != null) {
            sb.append("OBX");
            // OBX-1
            sb.append("|");
            sb.append(obxSetId);
            // OBX-2
            sb.append("|DT");
            // OBX-3
            sb.append("|");
            sb.append(loinc).append("^").append(loincLabel).append("^LN");
            // OBX-4
            sb.append("|");
            sb.append(obsSubId);
            // OBX-5
            sb.append("|");
            sb.append(formatDate(date));
            // OBX-6
            sb.append("|");
            // OBX-7
            sb.append("|");
            // OBX-8
            sb.append("|");
            // OBX-9
            sb.append("|");
            // OBX-10
            sb.append("|");
            // OBX-11
            sb.append("|");
            sb.append("F");
            sb.append("|||");
            sb.append(formatDate(observationDate));
            sb.append("\r");
        }
    }

    public void printObx(StringBuilder sb, int obxSetId, int obsSubId, Date observationDate, String loinc, String loincLabel, String value, CodesetType codesetType, String valueTable, String method) {
        CodeMap codeMap = codeMapManager.getCodeMap();

        String valueLabel = "";
        Code code = codeMap.getCodeForCodeset(codesetType, value);
        if (code != null) {
            valueLabel = StringUtils.defaultIfBlank(code.getLabel(), "").replaceAll("[\\t\\n\\r]+", " ");
        }
        printObx(sb, obxSetId, obsSubId, observationDate, loinc, loincLabel, value, valueLabel, valueTable, method);
    }

    public void printObx(StringBuilder sb, int obxSetId, int obsSubId, Date observationDate, String loinc,
                         String loincLabel, String value, String valueLabel, String valueTable, String method) {
        sb.append("OBX");
        // OBX-1
        sb.append("|");
        sb.append(obxSetId);
        // OBX-2
        sb.append("|");
        sb.append("CE");
        // OBX-3
        sb.append("|");
        sb.append(loinc).append("^").append(loincLabel).append("^LN");
        // OBX-4
        sb.append("|");
        sb.append(obsSubId);
        // OBX-5
        sb.append("|");
        sb.append(value).append("^").append(valueLabel).append("^").append(valueTable);
        // OBX-6
        sb.append("|");
        // OBX-7
        sb.append("|");
        // OBX-8
        sb.append("|");
        // OBX-9
        sb.append("|");
        // OBX-10
        sb.append("|");
        // OBX-11
        sb.append("|");
        sb.append("F");
        // OBX-12
        sb.append("|");
        // OBX-13
        sb.append("|");
        // OBX-14
        sb.append("|");
        sb.append(formatDate(observationDate));
        // OBX-15
        sb.append("|");
        // OBX-16
        sb.append("|");
        // OBX-17
        sb.append("|");
        sb.append(StringUtils.defaultIfBlank(method, ""));
        sb.append("\r");
    }

    public void printORC(StringBuilder sb, Facility facility, VaccinationEvent vaccinationEvent) {
        sb.append("ORC");
        // ORC-1
        sb.append("|RE");
        // ORC-2
        sb.append("|");
        sb.append(vaccinationEvent.getId()).append("^IIS");
        // ORC-3
        sb.append("|"); // TODO verify
        if (facility != null) {
            sb.append(facility.getId()).append("^").append(facility.getNameDisplay());
        }
        // ORC-4
        sb.append("|");
        // ORC-5
        sb.append("|");
        // ORC-6
        sb.append("|");
        // ORC-7
        sb.append("|");
        // ORC-8
        sb.append("|");
        // ORC-9
        sb.append("|");
        // ORC-10
        sb.append("|");
        if (vaccinationEvent.getEnteringClinician() != null) {
            printXCN(sb, vaccinationEvent.getEnteringClinician());
        }
        // ORC-11
        sb.append("|");
        // ORC-12
        sb.append("|");
        if (vaccinationEvent.getOrderingClinician() != null) {
            printXCN(sb, vaccinationEvent.getOrderingClinician());
        }
        sb.append("\r");
    }

    public void printObx(StringBuilder sb, int obxSetId, int obsSubId,
                         Observation ob) {
        sb.append("OBX");
        // OBX-1
        sb.append("|");
        sb.append(obxSetId);
        // OBX-2
        sb.append("|");
        sb.append(ob.getValueType());
        // OBX-3
        sb.append("|");
        sb.append(ob.getIdentifierCode()).append("^").append(ob.getIdentifierLabel()).append("^").append(ob.getIdentifierTable());
        // OBX-4
        sb.append("|");
        sb.append(obsSubId);
        // OBX-5
        sb.append("|");
        if (ob.getValueTable().equals("")) {
            sb.append(ob.getValueCode());
        } else {
            sb.append(ob.getValueCode()).append("^").append(ob.getValueLabel()).append("^").append(ob.getValueTable());
        }
        // OBX-6
        sb.append("|");
        if (ob.getUnitsTable().equals("")) {
            sb.append(ob.getUnitsCode());
        } else {
            sb.append(ob.getUnitsCode()).append("^").append(ob.getUnitsLabel()).append("^").append(ob.getUnitsTable());
        }
        // OBX-7
        sb.append("|");
        // OBX-8
        sb.append("|");
        // OBX-9
        sb.append("|");
        // OBX-10
        sb.append("|");
        // OBX-11
        sb.append("|");
        sb.append(ob.getResultStatus());
        // OBX-12
        sb.append("|");
        // OBX-13
        sb.append("|");
        // OBX-14
        sb.append("|");
        if (ob.getObservationDate() != null) {
            sb.append(formatDate(ob.getObservationDate()));
        }
        // OBX-15
        sb.append("|");
        // OBX-16
        sb.append("|");
        // OBX-17
        sb.append("|");
        if (ob.getMethodTable().equals("")) {
            sb.append(ob.getMethodCode());
        } else {
            sb.append(ob.getMethodCode()).append("^").append(ob.getMethodLabel()).append("^").append(ob.getMethodTable());
        }
        sb.append("\r");
    }


    public void printPD1(StringBuilder sb, EhrPatient patient) {
        sb.append("PD1");
        // PD1-1
        sb.append("|");
        // PD1-2
        sb.append("|");
        // PD1-3
        sb.append("|");
        // PD1-4
        sb.append("|");
        if (patient.getGeneralPractitioner() != null) {
            printXCN(sb, patient.getGeneralPractitioner());
        }
        // PD1-5
        sb.append("|");
        // PD1-6
        sb.append("|");
        // PD1-7
        sb.append("|");
        // PD1-8
        sb.append("|");
        // PD1-9
        sb.append("|");
        // PD1-10
        sb.append("|");
        // PD1-11
        if (patient.getPublicityIndicator() != null) {
            sb.append(patient.getPublicityIndicator());
        }
        sb.append("|");
        // PD1-12
        if (patient.getProtectionIndicator() != null) {
            sb.append(patient.getProtectionIndicator());
        }
        sb.append("|");
        // PD1-13
        if (patient.getProtectionIndicatorDate() != null) {
            sb.append(formatDate(patient.getProtectionIndicatorDate()));
        }
        sb.append("|");
        // PD1-14
        sb.append("|");
        // PD1-15
        sb.append("|");
        // PD1-16
        if (patient.getRegistryStatusIndicator() != null) {
            sb.append(patient.getRegistryStatusIndicator());
        }
        sb.append("|");
        // PD1-17
        if (patient.getRegistryStatusIndicatorDate() != null) {
            sb.append(formatDate(patient.getRegistryStatusIndicatorDate()));
        }
        sb.append("|");
        // PD1-18
        if (patient.getPublicityIndicatorDate() != null) {
            sb.append(formatDate(patient.getPublicityIndicatorDate()));
        }
        sb.append("|");
        // PD1-19
        sb.append("|");
        // PD1-20
        sb.append("|");
        // PD1-21
        sb.append("|");
    }

    // Extended Address
    private void printXAD(StringBuilder sb, EhrAddress ehrAddress) {
        sb.append(ehrAddress.getAddressLine1()).append("^").append(ehrAddress.getAddressLine2()).append("^").append(ehrAddress.getAddressCity()).append("^").append(ehrAddress.getAddressState()).append("^").append(ehrAddress.getAddressZip()).append("^").append(ehrAddress.getAddressCountry()).append("^").append("P");
    }

    // Extended Telephone number
    private void printXTN(StringBuilder sb, EhrPhoneNumber ehrPhoneNumber) {
        sb.append("^");
        sb.append(StringUtils.defaultIfBlank(ehrPhoneNumber.getUse(), ""));
        sb.append("^");
        sb.append(StringUtils.defaultIfBlank(ehrPhoneNumber.getType(), ""));
        sb.append("^^^");
        String number = ehrPhoneNumber.getNumber().replaceAll("[()\\s-]", "").strip();
        if (number.length() > 3) {
            sb.append(number, 0, 3).append("^").append(number.substring(3));
        }
    }

    // XTN for email
    private void printXtnEmail(StringBuilder sb, String email) {
        sb.append("^NET^X.400^").append(StringUtils.defaultIfBlank(email, ""));
    }


    // Extended Clinician
    private void printXCN(StringBuilder sb, Clinician clinician) {
        if (clinician != null) {
            EhrIdentifier ehrIdentifier = clinician.getIdentifiers().stream().findFirst().orElse(new EhrIdentifier());
            sb.append(StringUtils.defaultIfBlank(ehrIdentifier.getValue(), "")).append("^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameLast(), "")).append("^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameFirst(), "")).append("^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameMiddle(), "")).append("^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameSuffix(), "")).append("^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNamePrefix(), "")).append("^");
            sb.append(StringUtils.defaultIfBlank(clinician.getQualification(), "")).append("^");
        }
    }

    public void printCode(StringBuilder sb, Code code, String tableName) {
        if (code != null) {
            String label = StringUtils.defaultIfBlank(code.getLabel(), "").replaceAll("[\\t\\n\\r]+", " ");
            sb.append(code.getValue()).append("^").append(label).append("^").append(StringUtils.defaultIfBlank(tableName, ""));
        }
    }

    public void printCode(StringBuilder sb, String value, CodesetType codesetType, String tableName) {
        CodeMap codeMap = codeMapManager.getCodeMap();
        if (StringUtils.isNotBlank(value)) {
            Code code = codeMap.getCodeForCodeset(codesetType, value);
            if (code != null) {
                printCode(sb, code, tableName);
            } else {
                sb.append(value).append("^^").append(StringUtils.defaultIfBlank(tableName, ""));
            }
        }
    }

    private static void printName(StringBuilder sb, String firstName, String middleName, String lastName) {
        sb.append(lastName).append("^").append(firstName).append("^").append(middleName).append("^^^^L");
    }

    private static void printName(StringBuilder sb, EhrHumanName ehrHumanName) {
        sb.append(ehrHumanName.getNameLast()).append("^")
                .append(ehrHumanName.getNameFirst()).append("^")
                .append(ehrHumanName.getNameMiddle()).append("^")
                .append(ehrHumanName.getNameSuffix()).append("^")
                .append(ehrHumanName.getNamePrefix()).append("^")
                .append("^");
        if (!ProcessingFlavor.LIGUAL.isActive()) {
            sb.append(ehrHumanName.getNameType());
        } else {
            sb.append("L");
        }
    }

    private static Integer increment = 1;

    private static int nextIncrement() {
        synchronized (increment) {
            if (increment < Integer.MAX_VALUE) {
                increment = increment + 1;
            } else {
                increment = 1;
            }
            return increment;
        }
    }

}