package org.immregistries.ehr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.hibernate.Query;
import org.hibernate.Session;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Observation;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;
import org.immregistries.ehr.servlet.PopServlet;
import org.immregistries.iis.kernal.model.CodeMapManager;
import org.immregistries.mqe.hl7util.parser.HL7Reader;

public class HL7printer {

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


  public String buildHL7(Patient vaccination) {
    StringBuilder sb = new StringBuilder();
    CodeMap codeMap = new CodeMap();
    Patient patient = vaccination;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    createMSH("VXU^VO4^VXU_V04", "Tom", sb);
    sb.append(printQueryPID(patient, new StringBuilder(), sdf, 1) + "\n");
    sb.append(printQueryNK1(patient, new StringBuilder(), codeMap));

    return sb.toString();
  }

  public String buildVxu(Vaccine vaccination,Patient patient,Facility facility) {
    StringBuilder sb = new StringBuilder();
    CodeMap codeMap = CodeMapManager.getCodeMap();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    /*HL7Reader reader = new HL7Reader(
        "MSH|^~\\&|||AIRA|EHR Sandbox|20120701082240-0500||VXU^V04^VXU_V04|NIST-IZ-001.00|P|2.5.1|||ER|AL|||||Z22^CDCPHINVS\r");*/
    createMSH("VXU^V04^VXU_V04", "Z22", sb);
    printQueryPID(patient, sb, sdf, 1);
    printQueryNK1(patient, sb, codeMap);
  
    int obxSetId = 0;
    int obsSubId = 0;
    {
      Code cvxCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_CVX_CODE,
          vaccination.getVaccineCvxCode());
      System.out.println(cvxCode);
      System.out.println(vaccination.getVaccineCvxCode());
      if (cvxCode != null) {
        printORC(facility, sb, vaccination/*, vaccinationReported, originalReporter*/);
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
        if (!vaccination.getVaccineNdcCode().equals("")) {
          Code ndcCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_NDC_CODE,
              vaccination.getVaccineNdcCode());
          if (ndcCode != null) {
            sb.append("~" + ndcCode.getValue() + "^" + ndcCode.getLabel() + "^NDC");
          }
        }
        {
          // RXA-6
          sb.append("|");
          double adminAmount = 0.0;
          if (!vaccination.getAdministeredAmount().equals("")) {
            try {
              adminAmount = Double.parseDouble(vaccination.getAdministeredAmount());
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
          if (vaccination.getInformationSource() != null) {
            informationCode = codeMap.getCodeForCodeset(CodesetType.VACCINATION_INFORMATION_SOURCE,
                vaccination.getInformationSource());
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
        /*if (vaccinationReported.getOrgLocation() == null
            || vaccinationReported.getOrgLocation().getOrgFacilityCode() == null
            || "".equals(vaccinationReported.getOrgLocation().getOrgFacilityCode())) {
          sb.append("AIRA");
        } else {
          sb.append(vaccinationReported.getOrgLocation().getOrgFacilityCode());
        }*/
        // RXA-12
        sb.append("|");
        // RXA-13
        sb.append("|");
        // RXA-14
        sb.append("|");
        // RXA-15
        sb.append("|");
        if (vaccination.getLotnumber() != null) {
          sb.append(vaccination.getLotnumber());
        }
        // RXA-16
        sb.append("|");
        if (vaccination.getExpirationDate() != null) {
          sb.append(sdf.format(vaccination.getExpirationDate()));
        }
        // RXA-17
        sb.append("|");
        sb.append(printCode(vaccination.getVaccineMvxCode(),
            CodesetType.VACCINATION_MANUFACTURER_CODE, "MVX", codeMap));
        // RXA-18
        sb.append("|");
        sb.append(printCode(vaccination.getRefusalReasonCode(),
            CodesetType.VACCINATION_REFUSAL, "NIP002", codeMap));
        // RXA-19
        sb.append("|");
        // RXA-20
        sb.append("|");
        /*if (!processingFlavorSet.contains(ProcessingFlavor.LIME)) {*/
          String completionStatus = vaccination.getCompletionStatus();
          if (completionStatus == null || completionStatus.equals("")) {
            completionStatus = "CP";
          }
          sb.append(printCode(completionStatus, CodesetType.VACCINATION_COMPLETION, null, codeMap));
        /*}*/
  
        // RXA-21
        String actionCode = vaccination.getActionCode();
        if (actionCode == null || actionCode.equals("")
            || (!actionCode.equals("A") && !actionCode.equals("D"))) {
          actionCode = "A";
        }
        sb.append("|" );
        sb.append(vaccination.getActionCode());
        sb.append("\r");
        if (vaccination.getBodyRoute() != null
            && !vaccination.getBodyRoute().equals("")) {
          sb.append("RXR");
          // RXR-1
          sb.append("|");
          sb.append(printCode(vaccination.getBodyRoute(), CodesetType.BODY_ROUTE, "NCIT",
              codeMap));
          // RXR-2
          sb.append("|");
          sb.append(printCode(vaccination.getBodySite(), CodesetType.BODY_SITE, "HL70163",
              codeMap));
          sb.append("\r");
        }
        /*TestEvent testEvent = vaccinationReported.getTestEvent();
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
        }*/
        {
          /*Session dataSession = PopServlet.getDataSession();
          Query query = dataSession.createQuery(
              "from ObservationMaster where patient = :patient and vaccination = :vaccination");
          query.setParameter("patient", patient);
          query.setParameter("vaccination", vaccination);
          List<Observation> observationList = query.list();
          if (observationList.size() > 0) {
            obsSubId++;
            for (Observation observation : observationList) {
              obxSetId++;
              printObx(sb, obxSetId, obsSubId, observation);
            }
          }*/
        }
      }
    }
    return sb.toString();
  }
  
  public String printQueryNK1(Patient patient, StringBuilder sb, CodeMap codeMap) {
    if (patient != null) {
      if (!patient.getGuardianRelationship().equals("")
          && !(patient.getGuardianLast()== null ? "": patient.getGuardianLast()).equals("")
          && !(patient.getGuardianFirst()== null ? "": patient.getGuardianFirst()).equals("")) {
        Code code = codeMap.getCodeForCodeset(CodesetType.PERSON_RELATIONSHIP,
            patient.getGuardianRelationship());
        if (code != null) {
          sb.append("NK1");
          sb.append("|1");
          sb.append("|" + patient.getGuardianLast() == null ? ""
              : patient.getGuardianLast() + "^" + patient.getGuardianFirst() == null
                  ? ""
                  : patient.getGuardianFirst() + "^^^^^L");
          sb.append("|" + code.getValue() + "^" + code.getLabel() + "^HL70063");
          sb.append("\r");
        }
      }
    }
    return sb.toString();
  }

  public String printQueryPID(Patient patient, StringBuilder sb, 
      SimpleDateFormat sdf, int pidCount) {
    // PID
    sb.append("PID");
    // PID-1
    sb.append("|" + pidCount);
    // PID-2
    sb.append("|");
    // PID-3
    sb.append("|" + patient.getPatientId() + "^^^EHR^MR");
    /*if (patient != null) {
      sb.append("~" + patient.getExternalLink() + "^^^"
          + patient.getAuthority() + "^"
          + patient.getType());
    }*/
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
        String race = patient.getRace();
        // PID-11
        sb.append("|" + patient.getAddressLine1() + "^" + patient.getAddressLine2()
            + "^" + patient.getAddressCity() + "^" + patient.getAddressState() + "^"
            + patient.getAddressZip() + "^" + patient.getAddressCountry() + "^"+"P");
        // PID-12
        sb.append("|");
        // PID-13
        sb.append("|");
        String phone = patient.getPhone();
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
        sb.append(patient.getBirthFlag()== null ? "" :patient.getBirthFlag());
        // PID-25
        sb.append("|");
        sb.append(patient.getBirthOrder()== null ? "" : patient.getBirthOrder());

      }
      sb.append("\r");
    }
    return sb.toString();
  }

  public void createMSH(String messageType, String profileId, StringBuilder sb) {
    String sendingApp = "";
    String sendingFac = "";
    String receivingApp = "";
    String receivingFac = "EHR Sandbox";

    receivingFac += " v" + SoftwareVersion.VERSION;


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
    // build MSH
    sb.append("MSH|^~\\&|");
    sb.append(receivingApp + "|");
    sb.append(receivingFac + "|");
    sb.append(sendingApp + "|");
    sb.append(sendingFac + "|");
    sb.append(sendingDateString + "|");
    sb.append("|");
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


  public void printObx(StringBuilder sb, int obxSetId, int obsSubId, String loinc,
      String loincLabel, Date value) {
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

  /*public String buildAck(HL7Reader reader, List<ProcessingException> processingExceptionList) {
    StringBuilder sb = new StringBuilder();
    {
      String messageType = "ACK^V04^ACK";
      String profileId = Z23_ACKNOWLEDGEMENT;
      createMSH(messageType, profileId, sb);
    }
  
    String sendersUniqueId = "";
    reader.resetPostion();
    if (reader.advanceToSegment("MSH")) {
      sendersUniqueId = reader.getValue(10);
    } else {
      sendersUniqueId = "MSH NOT FOUND";
    }
    if (sendersUniqueId.equals("")) {
      sendersUniqueId = "MSH-10 NOT VALUED";
    }
    String overallStatus = "AA";
    for (ProcessingException pe : processingExceptionList) {
      if (pe.isError() || pe.isWarning()) {
        overallStatus = "AE";
        break;
      }
    }
  
    sb.append("MSA|" + overallStatus + "|" + sendersUniqueId + "\r");
    for (ProcessingException pe : processingExceptionList) {
      printERRSegment(pe, sb);
    }
    return sb.toString();
  }*/

  /*public void printERRSegment(ProcessingException e, StringBuilder sb) {
    sb.append("ERR|");
    sb.append("|"); // 2
    if (e.getSegmentId() != null && !e.getSegmentId().equals("")) {
      sb.append(e.getSegmentId() + "^" + e.getSegmentRepeat());
      if (e.getFieldPosition() > 0) {
        sb.append("^" + e.getFieldPosition());
      }
    }
    sb.append("|101^Required field missing^HL70357"); // 3
    sb.append("|"); // 4
    if (e.isError()) {
      sb.append("E");
    } else if (e.isWarning()) {
      sb.append("W");
    } else if (e.isInformation()) {
      sb.append("I");
    }
    sb.append("|"); // 5
    sb.append("|"); // 6
    sb.append("|"); // 7
    sb.append("|" + e.getMessage()); // 8
    sb.append("|\r");
  }*/
  
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
  
  public void printORC(Facility orgAccess, StringBuilder sb, Vaccine vaccination/*,
      VaccinationEvent vaccinationReported, boolean originalReporter*/) {
    
    sb.append("ORC");
    // ORC-1
    sb.append("|RE");
    // ORC-2
    sb.append("|");
    if (vaccination != null) {
      sb.append(vaccination.getVaccineId() + "^IIS");
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
      /*if (originalReporter) {*/
        sb.append(orgAccess.getFacilityId() + "^"
            + orgAccess.getNameDisplay());
      /*}*/
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
}
