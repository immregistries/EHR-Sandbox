package org.immregistries.ehr;

import java.text.SimpleDateFormat;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.model.Patient;

public class HL7printer {
  public void printQueryNK1(Patient patientReported, StringBuilder sb, CodeMap codeMap) {
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
  }

  public void printQueryPID(Patient patientReported, StringBuilder sb, Patient patient,
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
}
  }
