package org.immregistries.ehr.model;

import java.io.Serializable;
import java.util.Date;

public class Observation implements Serializable {

  private static final long serialVersionUID = 1L;

  private int observationReportedId = 0;
  private Patient patientReported = null;
  private VaccinationEvent vaccinationReported = null;
  private Date reportedDate = null;
  private Date updatedDate = null;
  private String valueType = "";
  private String identifierCode = "";
  private String identifierLabel = "";
  private String identifierTable = "";
  private String valueCode = "";
  private String valueLabel = "";
  private String valueTable = "";
  private String unitsCode = "";
  private String unitsLabel = "";
  private String unitsTable = "";
  private String resultStatus = "";
  private Date observationDate = null;
  private String methodCode = "";
  private String methodLabel = "";
  private String methodTable = "";

  public int getObservationReportedId() {
    return observationReportedId;
  }

  public void setObservationReportedId(int observationReportedId) {
    this.observationReportedId = observationReportedId;
  }

  public Patient getPatient() {
    return patientReported;
  }

  public void setPatientReported(Patient patientReported) {
    this.patientReported = patientReported;
  }

  public VaccinationEvent getVaccinationReported() {
    return vaccinationReported;
  }

  public void setVaccinationReported(VaccinationEvent vaccinationReported) {
    this.vaccinationReported = vaccinationReported;
  }


  public Date getReportedDate() {
    return reportedDate;
  }

  public void setReportedDate(Date reportedDate) {
    this.reportedDate = reportedDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }

  public String getIdentifierCode() {
    return identifierCode;
  }

  public void setIdentifierCode(String identifierCode) {
    this.identifierCode = identifierCode;
  }

  public String getIdentifierLabel() {
    return identifierLabel;
  }

  public void setIdentifierLabel(String identifierLabel) {
    this.identifierLabel = identifierLabel;
  }

  public String getIdentifierTable() {
    return identifierTable;
  }

  public void setIdentifierTable(String identifierTable) {
    this.identifierTable = identifierTable;
  }

  public String getValueCode() {
    return valueCode;
  }

  public void setValueCode(String valueCode) {
    this.valueCode = valueCode;
  }

  public String getValueLabel() {
    return valueLabel;
  }

  public void setValueLabel(String valueLabel) {
    this.valueLabel = valueLabel;
  }

  public String getValueTable() {
    return valueTable;
  }

  public void setValueTable(String valueTable) {
    this.valueTable = valueTable;
  }

  public String getUnitsCode() {
    return unitsCode;
  }

  public void setUnitsCode(String unitsCode) {
    this.unitsCode = unitsCode;
  }

  public String getUnitsLabel() {
    return unitsLabel;
  }

  public void setUnitsLabel(String unitsLabel) {
    this.unitsLabel = unitsLabel;
  }

  public String getUnitsTable() {
    return unitsTable;
  }

  public void setUnitsTable(String unitsTable) {
    this.unitsTable = unitsTable;
  }

  public Date getObservationDate() {
    return observationDate;
  }

  public void setObservationDate(Date observation_date) {
    this.observationDate = observation_date;
  }

  public String getMethodCode() {
    return methodCode;
  }

  public void setMethodCode(String methodCode) {
    this.methodCode = methodCode;
  }

  public String getMethodLabel() {
    return methodLabel;
  }

  public void setMethodLabel(String methodLabel) {
    this.methodLabel = methodLabel;
  }

  public String getMethodTable() {
    return methodTable;
  }

  public void setMethodTable(String methodTable) {
    this.methodTable = methodTable;
  }

  public String getResultStatus() {
    return resultStatus;
  }

  public void setResultStatus(String resultStatus) {
    this.resultStatus = resultStatus;
  }


}