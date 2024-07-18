package org.immregistries.ehr.logic;


import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.Identifier;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class HL7printer {

    Logger logger = LoggerFactory.getLogger(HL7printer.class);
    @Autowired
    ResourceIdentificationService resourceIdentificationService;
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
        createMSH("VXU^V04^VXU_V04", "Z22", sb, facility);
        printQueryPID(patient, sb, 1);
        printPD1(sb, patient);
        printQueryNK1(patient, sb, codeMap);

        int obxSetId = 0;
        int obsSubId = 0;
        if (vaccinationEvent != null) {
            Vaccine vaccine = vaccinationEvent.getVaccine();
            Code cvxCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_CVX_CODE, vaccine.getVaccineCvxCode());
            if (cvxCode != null) {
                sb.append("\n");
                printORC(facility, sb, vaccinationEvent);
                sb.append("RXA");
                // RXA-1
                sb.append("|0");
                // RXA-2
                sb.append("|1");
                // RXA-3
                sb.append("|" + formatDate(vaccine.getAdministeredDate()));
                // RXA-4
                sb.append("|");
                // RXA-5
                sb.append("|");
                printCode(cvxCode, "CVX", sb);
                if (StringUtils.isNotBlank(vaccine.getVaccineNdcCode())) {
                    Code ndcCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_NDC_CODE, vaccine.getVaccineNdcCode());
                    if (ndcCode != null) {
                        sb.append("~");
                        printCode(ndcCode, "NDC", sb);
                    }
                }
                {
                    // RXA-6
                    sb.append("|");
                    double adminAmount = 0.0;
                    if (!vaccine.getAdministeredAmount().equals("")) {
                        try {
                            adminAmount = Double.parseDouble(vaccine.getAdministeredAmount());
                        } catch (NumberFormatException nfe) {
                            adminAmount = 0.0;
                        }
                    }
                    if (adminAmount > 0) {
                        sb.append(adminAmount);
                    }
                    // RXA-7
                    sb.append("|");
                    if (adminAmount > 0) {
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
                        informationCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_INFORMATION_SOURCE,
                                vaccine.getInformationSource());
                    }
                    if (informationCode != null) {
                        printCode(informationCode, "NIP001", sb);
                    }
                }
                // RXA-10
                sb.append("|");
                printXCN(vaccinationEvent.getAdministeringClinician(), sb);
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
                printCode(vaccine.getVaccineMvxCode(), CodesetType.VACCINATION_MANUFACTURER_CODE, codeMap, "MVX", sb);
                // RXA-18
                sb.append("|");
                printCode(vaccine.getRefusalReasonCode(), CodesetType.VACCINATION_REFUSAL, codeMap, "NIP002", sb);
                // RXA-19
                sb.append("|");
                // RXA-20
                sb.append("|");
                String completionStatus = vaccine.getCompletionStatus();
                if (StringUtils.isBlank(completionStatus)) {
                    completionStatus = "CP";
                }
                printCode(completionStatus, CodesetType.VACCINATION_COMPLETION, codeMap, null, sb);

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
                    printCode(vaccine.getBodyRoute(), CodesetType.BODY_ROUTE, codeMap, "NCIT", sb);
                    // RXR-2
                    sb.append("|");
                    printCode(vaccine.getBodySite(), CodesetType.BODY_SITE, codeMap, "HL70163", sb);
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
                    printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, value, codeMap, CodesetType.FINANCIAL_STATUS_CODE, valueTable, method);
                }

                if (StringUtils.isNotBlank(vaccine.getFundingSource())) {
                    obxSetId++;
                    obsSubId++;
                    String loinc = "30963-3";
                    String loincLabel = "Vaccine funding source";
                    String valueTable = "CDCPHINVS";
                    String value = vaccine.getFundingSource();
                    printObx(sb, obxSetId, obsSubId, null, loinc, loincLabel, value, codeMap, CodesetType.FINANCIAL_STATUS_CODE, valueTable, "");
                }

//                obsSubId++;
//                {
//                    obxSetId++;
//                    String loinc = "59781-5";
//                    String loincLabel = "Dose validity";
//                    String value = vaccine.getAdministeredAmount(); //TODO CHECK
//                    String valueLabel = "";
//                    String valueTable = "99107";
//                    printObx(sb, obxSetId, obsSubId, loinc, loincLabel, value, valueLabel, valueTable);
//                }

                obsSubId++;
                // page 24 https://www.cdc.gov/vaccines/programs/iis/technical-guidance/downloads/hl7-clarification-r6.pdf
                if (StringUtils.isNotBlank(vaccine.getInformationStatement())) {
                    obxSetId++;
                    String loinc = "69764-9";
                    String loincLabel = "Document Type";
                    String value = vaccine.getInformationStatement();
                    String valueTable = "cdcgs1vis";
                    printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, value, codeMap, CodesetType.VACCINATION_VIS_DOC_TYPE, valueTable, "");
                } else {
                    {
                        obxSetId++;
                        String loinc = "30956-7";
                        String loincLabel = "Vaccine type";
                        String value = vaccine.getInformationStatementCvx();
                        String valueTable = "CVX";
                        printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, value, codeMap, CodesetType.VACCINATION_CVX_CODE, valueTable, "");
                    }
                    if (vaccine.getInformationStatementPublishedDate() != null) {
                        obxSetId++;
                        String loinc = "29768-9";
                        String loincLabel = "Date Vaccine Information Statement Published";
                        Date date = vaccine.getInformationStatementPublishedDate();
                        printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, date);
                    }
                }

                if (vaccine.getInformationStatementPresentedDate() != null) {
                    obxSetId++;
                    String loinc = "29769-7";
                    String loincLabel = "Date Vaccine Information Statement Presented";
                    Date date = vaccine.getInformationStatementPresentedDate();
                    printObx(sb, obxSetId, obsSubId, vaccine.getUpdatedDate(), loinc, loincLabel, date);
                }
            }
        }
        return sb.toString();
    }

    public String printQueryNK1(EhrPatient patient, StringBuilder sb, CodeMap codeMap) {
        // https://hl7-definition.caristix.com/v2/HL7v2.5/Segments/NK1
        int count = 0;
        for (NextOfKinRelationship nextOfKinRelationship : patient.getNextOfKinRelationships()) {
            NextOfKin nextOfKin = nextOfKinRelationship.getNextOfKin();
            if (nextOfKin != null) {
                count++;
                Code code = codeMap.getCodeForCodeset(CodesetType.PERSON_RELATIONSHIP,
                        StringUtils.defaultIfBlank(nextOfKinRelationship.getRelationshipKind(), ""));
                sb.append("\r");
                sb.append("NK1");
//                NK1-1
                sb.append("|" + count);
//                NK1-2
                sb.append("|" + StringUtils.defaultIfBlank(nextOfKin.getNameLast(), "") + "^" +
                        StringUtils.defaultIfBlank(nextOfKin.getNameFirst(), "") + "^" +
                        StringUtils.defaultIfBlank(nextOfKin.getNameMiddle(), "") + "^" +
                        StringUtils.defaultIfBlank(nextOfKin.getNameSuffix(), "") + "^" +
                        "^^L");
//                NK1-3
                sb.append("|");
                printCode(nextOfKinRelationship.getRelationshipKind(), CodesetType.PERSON_RELATIONSHIP, codeMap, "HL70063", sb);
//                NK1-4
//                Optional<EhrAddress> ehrAddress = nextOfKin.getAddresses().stream().findFirst();
                sb.append("|");
                nextOfKin.getAddresses().stream().findFirst().ifPresent(address -> printXAD(address, sb));
//                NK1-5
                sb.append("|");
                Optional<EhrPhoneNumber> ehrPhoneNumber = nextOfKin.getPhoneNumbers().stream().findFirst();
                if (ehrPhoneNumber.isPresent()) {
                    printXTN(ehrPhoneNumber.get(), sb);
                    if (StringUtils.isNotBlank(nextOfKin.getEmail())) {
                        sb.append("~");
                    }
                }
//                NK1-5[2]
                if (StringUtils.isNotBlank(nextOfKin.getEmail())) {
                    printXtnEmail(nextOfKin.getEmail(), sb);
                }
            }
        }
        return sb.toString();
    }

    public String printQueryPID(EhrPatient patient, StringBuilder sb, int pidCount) {
        // PID
        sb.append("PID");
        // PID-1
        sb.append("|" + pidCount);
        // PID-2
        sb.append("|");
        // PID-3
        EhrIdentifier mrn = patient.getMrnEhrIdentifier();
        if (mrn != null) {
            sb.append("|" + mrn.getValue() + "^^^" + StringUtils.defaultIfBlank(mrn.getSystem(), "") + "^MR");
        } else {
            sb.append("|" + patient.getId() + "^^^EHR^MR");
        }
        // PID-4
        sb.append("|");
        // PID-5
        String firstName = patient.getNameFirst();
        String middleName = patient.getNameMiddle();
        String lastName = patient.getNameLast();

        String dateOfBirth = patient.getBirthDate() == null ? "" : formatDate(patient.getBirthDate());


        sb.append("|" + lastName + "^" + firstName + "^" + middleName + "^^^^L");

        // PID-6
        sb.append("|");
        if (patient != null) {
            sb.append(patient.getMotherMaiden() == null ? ""
                    : patient.getMotherMaiden() + "^^^^^^M");
        }
        // PID-7
        sb.append("|" + dateOfBirth);
        if (patient != null) {
            // PID-8
            {
                String sex = patient.getSex();
                if (!sex.equals("F") && !sex.equals("M") && !sex.equals("X")) {
                    sex = "U";
                }
                sb.append("|" + sex);
            }
            // PID-9
            sb.append("|");
            // PID-10
            sb.append("|");
            {
                String race = "";
                if (!patient.getRaces().isEmpty()) {
                    race = patient.getRaces().stream().findFirst().get().getValue(); // TODO
                }
            }
            {
                // PID-11
                //TODO support multiple address ?
                sb.append("|");
                EhrAddress ehrAddress = patient.getAddresses().stream().findFirst().orElse(new EhrAddress());
                printXAD(ehrAddress, sb);
                // PID-12
                sb.append("|");
                // PID-13
                sb.append("|");
                Optional<EhrPhoneNumber> ehrPhoneNumber = patient.getPhones().stream().findFirst();
                if (ehrPhoneNumber.isPresent()) {
                    printXTN(ehrPhoneNumber.get(), sb);
                    if (StringUtils.isNotBlank(patient.getEmail())) {
                        sb.append('~');
                    }
                }
                if (StringUtils.isNotBlank(patient.getEmail())) {
                    printXtnEmail(patient.getEmail(), sb);
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
                    printCode(ethnicityCode, "CDCREC", sb);
                }
                // PID-23
                sb.append("|");
                // PID-24
                sb.append("|");
                sb.append(StringUtils.defaultIfBlank(patient.getBirthFlag(), ""));
                // PID-25
                sb.append("|");
                sb.append(StringUtils.defaultIfBlank(patient.getBirthOrder(), ""));
            }
            sb.append("\r");
        }
        return sb.toString();
    }

    public void createMSH(String messageType, String profileId, StringBuilder sb, Facility facility) {
        // TODO Check that this is accurate as previous implementation did not fit the MSH doc
        String sendingApp = "EHR Sandbox" + " v" + EhrApiApplication.VERSION;
        String sendingFacIdentifier;
        if (Objects.nonNull(facility)) {
            Identifier identifier = resourceIdentificationService.facilityIdentifier(facility);
            sendingFacIdentifier = StringUtils.defaultIfBlank(identifier.getAssigner().getReference(), "") + "^"
                    + StringUtils.defaultIfBlank(identifier.getValue(), "") + "^" +
                    StringUtils.defaultIfBlank(identifier.getType().getText(), "");
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
            uniqueId = "" + System.currentTimeMillis() + nextIncrement();
        }
        String production = "P";
        // build MSH TODO verify order
//      MSH.1 | MSH.2
        sb.append("MSH|^~\\&|");
//      MSH.3
        sb.append(sendingApp + "|");
//      MSH.4
        sb.append(sendingFacIdentifier + "|");
//      MSH.5
        sb.append(receivingApp + "|");
//      MSH.6
        sb.append(receivingFac + "|");
//      MSH.7
        sb.append(sendingDateString + "|");
//      MSH.8
        sb.append("|");
//      MSH.9
        sb.append(messageType + "|");
        sb.append(uniqueId + "|");
        sb.append(production + "|");
        sb.append("2.5.1|");
        sb.append("|");
        sb.append("|");
        sb.append("ER|");
        sb.append("AL|");
        sb.append("|");
        sb.append("|");
        sb.append("|");
        sb.append("|");
        sb.append(profileId + "^CDCPHINVS");
        if (Objects.nonNull(facility)) {
            sb.append("|" + StringUtils.defaultIfBlank(facility.getNameDisplay(), ""));
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
            sb.append(loinc + "^" + loincLabel + "^LN");
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

    public void printObx(StringBuilder sb, int obxSetId, int obsSubId, Date observationDate, String loinc, String loincLabel, String value, CodeMap codeMap, CodesetType codesetType, String valueTable, String method) {
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
        sb.append(loinc + "^" + loincLabel + "^LN");
        // OBX-4
        sb.append("|");
        sb.append(obsSubId);
        // OBX-5
        sb.append("|");
        sb.append(value + "^" + valueLabel + "^" + valueTable);
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

    public void printORC(Facility facility, StringBuilder sb, VaccinationEvent vaccinationEvent) {
        Vaccine vaccine = vaccinationEvent.getVaccine();
        sb.append("ORC");
        // ORC-1
        sb.append("|RE");
        // ORC-2
        sb.append("|");
        if (vaccine != null) {
            sb.append(vaccinationEvent.getId() + "^IIS");
        }
        // ORC-3
        sb.append("|");
        if (vaccinationEvent == null) {

        } else {
            sb.append(facility.getId() + "^"
                    + facility.getNameDisplay());
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
        printXCN(vaccinationEvent.getEnteringClinician(), sb);
        // ORC-11
        sb.append("|");
        // ORC-12
        sb.append("|");
        printXCN(vaccinationEvent.getOrderingClinician(), sb);
        sb.append("\r");
    }

    public void printObx(StringBuilder sb, int obxSetId, int obsSubId,
                         Observation observation) {
        Observation ob = observation;
        sb.append("OBX");
        // OBX-1
        sb.append("|");
        sb.append(obxSetId);
        // OBX-2
        sb.append("|");
        sb.append(ob.getValueType());
        // OBX-3
        sb.append("|");
        sb.append(
                ob.getIdentifierCode() + "^" + ob.getIdentifierLabel() + "^" + ob.getIdentifierTable());
        // OBX-4
        sb.append("|");
        sb.append(obsSubId);
        // OBX-5
        sb.append("|");
        if (ob.getValueTable().equals("")) {
            sb.append(ob.getValueCode());
        } else {
            sb.append(ob.getValueCode() + "^" + ob.getValueLabel() + "^" + ob.getValueTable());
        }
        // OBX-6
        sb.append("|");
        if (ob.getUnitsTable().equals("")) {
            sb.append(ob.getUnitsCode());
        } else {
            sb.append(ob.getUnitsCode() + "^" + ob.getUnitsLabel() + "^" + ob.getUnitsTable());
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
            sb.append(ob.getMethodCode() + "^" + ob.getMethodLabel() + "^" + ob.getMethodTable());
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
    private void printXAD(EhrAddress ehrAddress, StringBuilder sb) {
        sb.append(ehrAddress.getAddressLine1() + "^" + ehrAddress.getAddressLine2()
                + "^" + ehrAddress.getAddressCity() + "^" + ehrAddress.getAddressState() + "^"
                + ehrAddress.getAddressZip() + "^" + ehrAddress.getAddressCountry() + "^" + "P");
    }

    // Extended Telephone number
    private void printXTN(EhrPhoneNumber ehrPhoneNumber, StringBuilder sb) {
        sb.append("^");
        sb.append(StringUtils.defaultIfBlank(ehrPhoneNumber.getUse(), ""));
        sb.append("^");
        sb.append(StringUtils.defaultIfBlank(ehrPhoneNumber.getType(), ""));
        sb.append("^^^");
        String number = ehrPhoneNumber.getNumber().replaceAll("[()\\s-]", "").strip();
        if (number.length() > 3) {
            sb.append(number.substring(0, 3) + "^" + number.substring(3));
        }
    }

    // XTN for email
    private void printXtnEmail(String email, StringBuilder sb) {
        sb.append("^NET^X.400^" + StringUtils.defaultIfBlank(email, ""));
    }


    // Extended Clinician
    private void printXCN(Clinician clinician, StringBuilder sb) {
        if (clinician != null) {
            EhrIdentifier ehrIdentifier = clinician.getIdentifiers().stream().findFirst().orElse(new EhrIdentifier());
            sb.append(StringUtils.defaultIfBlank(ehrIdentifier.getValue(), "") + "^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameLast(), "") + "^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameFirst(), "") + "^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameMiddle(), "") + "^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNameSuffix(), "") + "^");
            sb.append(StringUtils.defaultIfBlank(clinician.getNamePrefix(), "") + "^");
            sb.append(StringUtils.defaultIfBlank(clinician.getQualification(), "") + "^");
        }
    }

    public void printCode(Code code, String tableName, StringBuilder sb) {
        String label = StringUtils.defaultIfBlank(code.getLabel(), "").replaceAll("[\\t\\n\\r]+", " ");
        if (code != null) {
            sb.append(code.getValue() + "^" + label + "^" +
                    StringUtils.defaultIfBlank(tableName, ""));
        }
    }

    public void printCode(String value, CodesetType codesetType, CodeMap codeMap, String tableName, StringBuilder sb) {
        if (StringUtils.isNotBlank(value)) {
            printCode(codeMap.getCodeForCodeset(codesetType, value), tableName, sb);
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