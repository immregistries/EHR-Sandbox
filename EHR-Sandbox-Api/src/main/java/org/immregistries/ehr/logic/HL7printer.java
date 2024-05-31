package org.immregistries.ehr.logic;


import org.hl7.fhir.r5.model.Identifier;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@Service
public class HL7printer {
    @Autowired
    ResourceIdentificationService resourceIdentificationService;
    @Autowired
    CodeMapManager codeMapManager;

    private static final String QBP_Z34 = "Z34";
    private static final String QBP_Z44 = "Z44";
    private static final String RSP_Z42_MATCH_WITH_FORECAST = "Z42";
    private static final String RSP_Z32_MATCH = "Z32";
    private static final String RSP_Z31_MULTIPLE_MATCH = "Z31";
    private static final String RSP_Z33_NO_MATCH = "Z33";
    private static final String Z23_ACKNOWLEDGEMENT = "Z23";
    private static final String QUERY_OK = "OK";
    private static final String QUERY_NOT_FOUND = "NF";
    private static final String QUERY_TOO_MANY = "TM";
    private static final String QUERY_APPLICATION_ERROR = "AE";

    public String buildVxu(Vaccine vaccine, EhrPatient patient, Facility facility) {
        StringBuilder sb = new StringBuilder();
        CodeMap codeMap = codeMapManager.getCodeMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        createMSH("VXU^V04^VXU_V04", "Z22", sb, facility);
        printQueryPID(patient, sb, sdf, 1);
        printPD1(patient, sb, sdf);
        printQueryNK1(patient, sb, codeMap);

        int obxSetId = 0;
        int obsSubId = 0;
        if (vaccine != null) {
            Code cvxCode = codeMap.getCodeForCodeset(
                    CodesetType.VACCINATION_CVX_CODE,
                    vaccine.getVaccineCvxCode());
            if (cvxCode != null) {
                printORC(facility, sb, vaccine);
                sb.append("RXA");
                // RXA-1
                sb.append("|0");
                // RXA-2
                sb.append("|1");
                // RXA-3
                sb.append("|" + sdf.format(vaccine.getAdministeredDate()));
                // RXA-4
                sb.append("|");
                // RXA-5
                sb.append("|" + cvxCode.getValue() + "^" + cvxCode.getLabel() + "^CVX");
                if (!vaccine.getVaccineNdcCode().equals("")) {
                    Code ndcCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_NDC_CODE,
                            vaccine.getVaccineNdcCode());
                    if (ndcCode != null) {
                        sb.append("~" + ndcCode.getValue() + "^" + ndcCode.getLabel() + "^NDC");
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
                        sb.append(informationCode.getValue() + "^" + informationCode.getLabel() + "^NIP001");
                    }
                }
                // RXA-10
                sb.append("|");
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
                    sb.append(sdf.format(vaccine.getExpirationDate()));
                }
                // RXA-17
                sb.append("|");
                sb.append(printCode(vaccine.getVaccineMvxCode(),
                        CodesetType.VACCINATION_MANUFACTURER_CODE, "MVX", codeMap));
                // RXA-18
                sb.append("|");
                sb.append(printCode(vaccine.getRefusalReasonCode(),
                        CodesetType.VACCINATION_REFUSAL, "NIP002", codeMap));
                // RXA-19
                sb.append("|");
                // RXA-20
                sb.append("|");
                String completionStatus = vaccine.getCompletionStatus();
                if (completionStatus == null || completionStatus.equals("")) {
                    completionStatus = "CP";
                }
                sb.append(printCode(completionStatus, CodesetType.VACCINATION_COMPLETION, null, codeMap));

                // RXA-21
                String actionCode = vaccine.getActionCode();
                if (actionCode == null || actionCode.equals("")
                        || (!actionCode.equals("A") && !actionCode.equals("D"))) {
                    actionCode = "A";
                }
                sb.append("|");
                sb.append(vaccine.getActionCode());
                sb.append("\r");
                if (vaccine.getBodyRoute() != null
                        && !vaccine.getBodyRoute().equals("")) {
                    sb.append("RXR");
                    // RXR-1
                    sb.append("|");
                    sb.append(printCode(vaccine.getBodyRoute(), CodesetType.BODY_ROUTE, "NCIT",
                            codeMap));
                    // RXR-2
                    sb.append("|");
                    sb.append(printCode(vaccine.getBodySite(), CodesetType.BODY_SITE, "HL70163",
                            codeMap));
                    sb.append("\r");
                }
                Code codeVacc = codeMap.getCodeForCodeset(CodesetType.VACCINATION_CVX_CODE, vaccine.getVaccineCvxCode());
                obsSubId++;
                obxSetId++;
                String loinc = "64994-7";
                String loincLabel = "Vaccine funding program eligibility category";
                String value = "V02";
                String valueLabel = "VFC eligible - Medicaid/Medicaid Managed Care";
                String valueTable = "HL70064";
                printObx(sb, obxSetId, obsSubId, loinc, loincLabel, value, valueLabel, valueTable);
                obxSetId++;
                loinc = "30956-7";
                loincLabel = "Vaccine type";
                value = codeVacc.getValue();
                valueLabel = codeVacc.getLabel();
                valueTable = "CVX";
                printObx(sb, obxSetId, obsSubId, loinc, loincLabel, value, valueLabel, valueTable);
                obxSetId++;
                loinc = "59781-5";
                loincLabel = "Dose validity";
                value = vaccine.getAdministeredAmount();
                valueLabel = value; //don't know what to put here
                valueTable = "99107";
                printObx(sb, obxSetId, obsSubId, loinc, loincLabel, value, valueLabel, valueTable);
            }
        }
        return sb.toString();
    }

    public String printQueryNK1(EhrPatient patient, StringBuilder sb, CodeMap codeMap) {
        if (patient != null) {
            if (!patient.getNextOfKinRelationships().isEmpty()) {
                NextOfKinRelationship nextOfKinRelationship = patient.getNextOfKinRelationships().stream().findFirst().get();
                NextOfKin nextOfKin = nextOfKinRelationship.getNextOfKin();
                if (nextOfKin != null) {
                    Code code = codeMap.getCodeForCodeset(CodesetType.PERSON_RELATIONSHIP,
                            (nextOfKinRelationship.getRelationshipKind() == null ? "" : nextOfKinRelationship.getRelationshipKind()));
                    if (code != null) {
                        sb.append("NK1");
                        sb.append("|1");
                        sb.append("|" + (nextOfKin.getNameLast() == null ? ""
                                : nextOfKin.getNameLast()) + "^" + (nextOfKin.getNameFirst() == null
                                ? ""
                                : nextOfKin.getNameFirst()) + "^^^^^L");
                        sb.append("|" + code.getValue() + "^" + code.getLabel() + "^HL70063");
                        sb.append("\r");
                    }
                }

                //TODO multiple $ more information
            }
        }
        return sb.toString();
    }

    public String printQueryPID(EhrPatient patient, StringBuilder sb,
                                SimpleDateFormat sdf, int pidCount) {
        // PID
        sb.append("PID");
        // PID-1
        sb.append("|" + pidCount);
        // PID-2
        sb.append("|");
        // PID-3
        if (!patient.getMrn().isBlank()) {
            sb.append("|" + patient.getMrn() + "^^^urns:mrn^MR");
        } else {
            sb.append("|" + patient.getId() + "^^^EHR^MR");
        }
        // PID-4
        sb.append("|");
        // PID-5
        String firstName = patient.getNameFirst();
        String middleName = patient.getNameMiddle();
        String lastName = patient.getNameLast();

        String dateOfBirth = patient.getBirthDate() == null ? "" : sdf.format(patient.getBirthDate());


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
                // PID-11
                //TODO support multiple address
                EhrAddress ehrAddress = patient.getAddresses().stream().findFirst().orElse(new EhrAddress());
                sb.append("|" + ehrAddress.getAddressLine1() + "^" + ehrAddress.getAddressLine2()
                        + "^" + ehrAddress.getAddressCity() + "^" + ehrAddress.getAddressState() + "^"
                        + ehrAddress.getAddressZip() + "^" + ehrAddress.getAddressCountry() + "^" + "P");

                // PID-12
                sb.append("|");
                // PID-13
                sb.append("|");
                String phone = patient.getPhones().stream().findFirst().orElse(new EhrPhoneNumber("")).getNumber();
                if (phone.length() == 10) { //TODO improve support
                    sb.append("^PRN^PH^^^" + phone.substring(0, 3) + "^" + phone.substring(3, 10));
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
        /*{
        String ethnicity = patient.getEthnicity();
        if (!ethnicity.equals("")) {
          if (processingFlavorSet.contains(ProcessingFlavor.PITAYA)
              || processingFlavorSet.contains(ProcessingFlavor.PERSIMMON)) {
            CodeMap codeMap = CodeMapManager.getCodeMap();
            Code ethnicityCode =
                codeMap.getCodeForCodeset(CodesetType.PATIENT_ETHNICITY, ethnicity);
            if (processingFlavorSet.contains(ProcessingFlavor.PITAYA) || (ethnicityCode != null
                && CodeStatusValue.getBy(ethnicityCode.getCodeStatus()) != CodeStatusValue.VALID)) {
              sb.append(ethnicityCode);
              sb.append("^");
              if (ethnicityCode != null) {
                sb.append(ethnicityCode.getDescription());
              }
              sb.append("^CDCREC");
            }
          }
        }
        }*/
                // PID-23
                sb.append("|");
                // PID-24
                sb.append("|");
                sb.append(patient.getBirthFlag() == null ? "" : patient.getBirthFlag());
                // PID-25
                sb.append("|");
                sb.append(patient.getBirthOrder() == null ? "" : patient.getBirthOrder());

            }
            sb.append("\r");
        }
        return sb.toString();
    }

    public void createMSH(String messageType, String profileId, StringBuilder sb, Facility facility) {
        // TODO Check that this is accurate as previous implementation did not fit the MSH doc
        String sendingApp = "EHR Sandbox" + " v" + EhrApiApplication.VERSION;
        String sendingFac;
        if (Objects.nonNull(facility)) {
            Identifier identifier = resourceIdentificationService.facilityIdentifier(facility);
            sendingFac = identifier.getValue();
//        "^" + identifier.getValue() + "^L,M,N";
        } else {
            sendingFac = "";
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
        sb.append(sendingFac + "|");
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
        sb.append(profileId + "^CDCPHINVS\r");
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

    public void printObx(StringBuilder sb, int obxSetId, int obsSubId, String loinc,
                         String loincLabel, String value) {
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
        sb.append(value);
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
        sb.append("\r");
    }


    public void printObx(StringBuilder sb, int obxSetId, int obsSubId, String loinc,
                         String loincLabel, String value, String valueLabel, String valueTable) {
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
        sb.append("\r");
    }


    public void printObx(StringBuilder sb, int obxSetId, int obsSubId, String loinc, String loincLabel, Date value) {
        sb.append("OBX");
        // OBX-1
        sb.append("|");
        sb.append(obxSetId);
        // OBX-2
        sb.append("|");
        sb.append("DT");
        // OBX-3
        sb.append("|");
        sb.append(loinc + "^" + loincLabel + "^LN");
        // OBX-4
        sb.append("|");
        sb.append(obsSubId);
        // OBX-5
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sb.append("|");
        if (value != null) {
            sb.append(sdf.format(value));
        }
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
        sb.append("\r");
    }

    public String printCode(String value, CodesetType codesetType, String tableName,
                            CodeMap codeMap) {
        if (value != null) {
            Code code = codeMap.getCodeForCodeset(codesetType, value);
            if (code != null) {
                if (tableName == null) {
                    return code.getValue();
                }
                return code.getValue() + "^" + code.getLabel() + "^" + tableName;
            }
        }
        return "";
    }


    private static final Random random = new Random();
    private static final char[] ID_CHARS =
            {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T',
                    'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};


    public String generateId() {
        String patientRegistryId = "";
        for (int i = 0; i < 12; i++) {
            patientRegistryId += ID_CHARS[random.nextInt(ID_CHARS.length)];
        }
        return patientRegistryId;
    }

    public void printORC(Facility facility, StringBuilder sb, Vaccine vaccination/*,
      VaccinationEvent vaccinationReported, boolean originalReporter*/) {

        sb.append("ORC");
        // ORC-1
        sb.append("|RE");
        // ORC-2
        sb.append("|");
        if (vaccination != null) {
            sb.append(vaccination.getId() + "^IIS");
        }
        // ORC-3
        sb.append("|");
        if (vaccination == null) {
      /*if (processingFlavorSet.contains(ProcessingFlavor.LIME)) {
        sb.append("999^IIS");
      } else {
        sb.append("9999^IIS");
      }*/
        } else {
            sb.append(facility.getId() + "^"
                    + facility.getNameDisplay());
        }
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sb.append(sdf.format(ob.getObservationDate()));
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


    public void printPD1(EhrPatient patient, StringBuilder sb,
                         SimpleDateFormat sdf) {
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
            sb.append(sdf.format(patient.getProtectionIndicatorDate()));
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
            sb.append(sdf.format(patient.getRegistryStatusIndicatorDate()));
        }
        sb.append("|");
        // PD1-18
        if (patient.getPublicityIndicatorDate() != null) {
            sb.append(sdf.format(patient.getPublicityIndicatorDate()));
        }
        sb.append("|");
        // PD1-19
        sb.append("|");
        // PD1-20
        sb.append("|");
        // PD1-21
        sb.append("|");
    }


}