package org.immregistries.ehr;

import java.text.SimpleDateFormat;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.VaccinationEvent;

public class HL7printer {
  public String buildHL7(Patient vaccination) {
    StringBuilder sb = new StringBuilder();
    CodeMap codeMap = new CodeMap();
    Patient patientReported = vaccination;
    Patient patient = vaccination; //à retoucher
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    sb.append(printQueryPID(patientReported, new StringBuilder(), patient, sdf, 1));
    sb.append(printQueryNK1(patientReported, new StringBuilder(), codeMap));

    return sb.toString();
  }
  /*public String buildVxu(VaccinationReported vaccinationReported, OrgAccess orgAccess) {
    StringBuilder sb = new StringBuilder();
    CodeMap codeMap = CodeMapManager.getCodeMap();
    Set<ProcessingFlavor> processingFlavorSet = orgAccess.getOrg().getProcessingFlavorSet();
    PatientReported patientReported = vaccinationReported.getPatientReported();
    PatientMaster patient = patientReported.getPatient();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    HL7Reader reader = new HL7Reader(
        "MSH|^~\\&|||AIRA|IIS Sandbox|20120701082240-0500||VXU^V04^VXU_V04|NIST-IZ-001.00|P|2.5.1|||ER|AL|||||Z22^CDCPHINVS\r");
    createMSH("VXU^V04^VXU_V04", "Z22", reader, sb, processingFlavorSet);
    printQueryPID(patientReported, processingFlavorSet, sb, patient, sdf, 1);
    printQueryNK1(patientReported, sb, codeMap);

    int obxSetId = 0;
    int obsSubId = 0;
    {
      VaccinationMaster vaccination = vaccinationReported.getVaccination();
      Code cvxCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_CVX_CODE,
          vaccination.getVaccineCvxCode());
      if (cvxCode != null) {

        boolean originalReporter =
            vaccinationReported.getPatientReported().getOrgReported().equals(orgAccess.getOrg());
        printORC(orgAccess, sb, vaccination, vaccinationReported, originalReporter);
        sb.append("RXA");
        // RXA-1
        sb.append("|0");
        // RXA-2
        sb.append("|1");
        // RXA-3
        sb.append("|" + sdf.format(vaccination.getAdministeredDate()));
        // RXA-4
        sb.append("|");
        // RXA-5
        sb.append("|" + cvxCode.getValue() + "^" + cvxCode.getLabel() + "^CVX");
        if (!vaccinationReported.getVaccineNdcCode().equals("")) {
          Code ndcCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_NDC_CODE,
              vaccinationReported.getVaccineNdcCode());
          if (ndcCode != null) {
            sb.append("~" + ndcCode.getValue() + "^" + ndcCode.getLabel() + "^NDC");
          }
        }
        {
          // RXA-6
          sb.append("|");
          double adminAmount = 0.0;
          if (!vaccinationReported.getAdministeredAmount().equals("")) {
            try {
              adminAmount = Double.parseDouble(vaccinationReported.getAdministeredAmount());
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
          if (vaccinationReported.getInformationSource() != null) {
            informationCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_INFORMATION_SOURCE,
                vaccinationReported.getInformationSource());
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
        if (vaccinationReported.getOrgLocation() == null
            || vaccinationReported.getOrgLocation().getOrgFacilityCode() == null
            || "".equals(vaccinationReported.getOrgLocation().getOrgFacilityCode())) {
          sb.append("AIRA");
        } else {
          sb.append(vaccinationReported.getOrgLocation().getOrgFacilityCode());
        }
        // RXA-12
        sb.append("|");
        // RXA-13
        sb.append("|");
        // RXA-14
        sb.append("|");
        // RXA-15
        sb.append("|");
        if (vaccinationReported.getLotnumber() != null) {
          sb.append(vaccinationReported.getLotnumber());
        }
        // RXA-16
        sb.append("|");
        if (vaccinationReported.getExpirationDate() != null) {
          sb.append(sdf.format(vaccinationReported.getExpirationDate()));
        }
        // RXA-17
        sb.append("|");
        sb.append(printCode(vaccinationReported.getVaccineMvxCode(),
            CodesetType.VACCINATION_MANUFACTURER_CODE, "MVX", codeMap));
        // RXA-18
        sb.append("|");
        sb.append(printCode(vaccinationReported.getRefusalReasonCode(),
            CodesetType.VACCINATION_REFUSAL, "NIP002", codeMap));
        // RXA-19
        sb.append("|");
        // RXA-20
        sb.append("|");
        if (!processingFlavorSet.contains(ProcessingFlavor.LIME)) {
          String completionStatus = vaccinationReported.getCompletionStatus();
          if (completionStatus == null || completionStatus.equals("")) {
            completionStatus = "CP";
          }
          sb.append(printCode(completionStatus, CodesetType.VACCINATION_COMPLETION, null, codeMap));
        }

        // RXA-21
        String actionCode = vaccinationReported.getActionCode();
        if (actionCode == null || actionCode.equals("")
            || (!actionCode.equals("A") && !actionCode.equals("D"))) {
          actionCode = "A";
        }
        sb.append("|" + vaccinationReported.getActionCode());
        sb.append("\r");
        if (vaccinationReported.getBodyRoute() != null
            && !vaccinationReported.getBodyRoute().equals("")) {
          sb.append("RXR");
          // RXR-1
          sb.append("|");
          sb.append(printCode(vaccinationReported.getBodyRoute(), CodesetType.BODY_ROUTE, "NCIT",
              codeMap));
          // RXR-2
          sb.append("|");
          sb.append(printCode(vaccinationReported.getBodySite(), CodesetType.BODY_SITE, "HL70163",
              codeMap));
          sb.append("\r");
        }
        TestEvent testEvent = vaccinationReported.getTestEvent();
        if (testEvent != null && testEvent.getEvaluationActualList() != null) {
          for (EvaluationActual evaluationActual : testEvent.getEvaluationActualList()) {
            obsSubId++;
            {
              obxSetId++;
              String loinc = "30956-7";
              String loincLabel = "Vaccine type";
              String value = evaluationActual.getVaccineCvx();
              String valueLabel = evaluationActual.getVaccineCvx();
              String valueTable = "CVX";
              printObx(sb, obxSetId, obsSubId, loinc, loincLabel, value, valueLabel, valueTable);
            }
            {
              obxSetId++;
              String loinc = "59781-5";
              String loincLabel = "Dose validity";
              String value = evaluationActual.getDoseValid();
              String valueLabel = value;
              String valueTable = "99107";
              printObx(sb, obxSetId, obsSubId, loinc, loincLabel, value, valueLabel, valueTable);
            }
          }
        }
        {
          Query query = dataSession.createQuery(
              "from ObservationMaster where patient = :patient and vaccination = :vaccination");
          query.setParameter("patient", patient);
          query.setParameter("vaccination", vaccination);
          List<ObservationMaster> observationList = query.list();
          if (observationList.size() > 0) {
            obsSubId++;
            for (ObservationMaster observation : observationList) {
              obxSetId++;
              printObx(sb, obxSetId, obsSubId, observation);
            }
          }
        }
      }
    }
    return sb.toString();
  }*/
  public String printQueryNK1(Patient patientReported, StringBuilder sb, CodeMap codeMap) {
    if (patientReported != null) {
      if (!patientReported.getGuardianRelationship().equals("")
          && !patientReported.getGuardianLast().equals("")
          && !patientReported.getGuardianFirst().equals("")) {
        Code code = codeMap.getCodeForCodeset(CodesetType.PERSON_RELATIONSHIP,
            patientReported.getGuardianRelationship());
        if (code != null) {
          sb.append("NK1");
          sb.append("|1");
          sb.append("|" + patientReported.getGuardianLast() + "^"
              + patientReported.getGuardianFirst() + "^^^^^L");
          sb.append("|" + code.getValue() + "^" + code.getLabel() + "^HL70063");
          sb.append("\r");
        }
      }
    }
    return sb.toString();
  }

  public String printQueryPID(Patient patientReported, StringBuilder sb, Patient patient,
      SimpleDateFormat sdf, int pidCount) {
    // PID
    sb.append("PID");
    // PID-1
    sb.append("|" + pidCount);
    // PID-2
    sb.append("|");
    // PID-3
    sb.append("|" + /*patient.getExternalLink() +*/ "^^^EHR^SR");
    /*if (patientReported != null) {
      sb.append("~" + patientReported.getExternalLink() + "^^^"
          + patientReported.getAuthority() + "^"
          + patientReported.getType());
    }*/
    // PID-4
    sb.append("|");
    // PID-5
    String firstName = patient.getNameFirst();
    String middleName = patient.getNameMiddle();
    String lastName = patient.getNameLast();
    String dateOfBirth = sdf.format(patient.getBirthDate());


    sb.append("|" + lastName + "^" + firstName + "^" + middleName + "^^^^L");

    // PID-6
    sb.append("|");
    if (patientReported != null) {
      sb.append(patientReported.getMotherMaiden() + "^^^^^^M");
    }
    // PID-7
    sb.append("|" + dateOfBirth);
    if (patientReported != null) {
      // PID-8
      {
        String sex = patientReported.getSex();
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
        String race = patientReported.getRace();
      // PID-11
      sb.append("|" + patientReported.getAddressLine1() + "^"
          + patientReported.getAddressLine2() + "^" + patientReported.getAddressCity()
          + "^" + patientReported.getAddressState() + "^"
          + patientReported.getAddressZip() + "^"
          + patientReported.getAddressCountry() + "^");
      // PID-12
      sb.append("|");
      // PID-13
      sb.append("|");
      String phone = patientReported.getPhone();
      if (phone.length() == 10) {
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
        String ethnicity = patientReported.getEthnicity();
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
      sb.append(patientReported.getBirthFlag());
      // PID-25
      sb.append("|");
      sb.append(patientReported.getBirthOrder());

    }
    sb.append("\r");
  }
    return sb.toString();
}

  }
