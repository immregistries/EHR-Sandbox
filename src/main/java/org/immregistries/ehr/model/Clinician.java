package org.immregistries.ehr.model;
import java.io.Serializable;
import java.util.Date;

public class Clinician implements Serializable {

  private static final long serialVersionUID = 1L;

  private int clinicianId = 0;
  private String nameLast = "";
  private String nameMiddle = "";
  private String nameFirst = "";

  public int getClinicianId() {
    return clinicianId;
  }

  public void setClinicianId(int clinicianId) {
    this.clinicianId = clinicianId;
  }

  public String getNameLast() {
    return nameLast;
  }

  public void setNameLast(String nameLast) {
    this.nameLast = nameLast;
  }

  public String getNameMiddle() {
    return nameMiddle;
  }

  public void setNameMiddle(String nameMiddle) {
    this.nameMiddle = nameMiddle;
  }

  public String getNameFirst() {
    return nameFirst;
  }

  public void setNameFirst(String nameFirst) {
    this.nameFirst = nameFirst;
  }

}