package org.immregistries.ehr.model;
import com.github.javafaker.Faker;

import java.io.Serializable;

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

  public static Clinician random(){
    Faker faker = new Faker();
    Clinician clinician = new Clinician();
    clinician.setNameFirst(faker.name().firstName());
    clinician.setNameLast(faker.name().lastName());
//    clinician.setNameMiddle(faker.name());

    return clinician;
  }

  public Clinician() {

  }

  public  void fillFromFullname(String fullName) {
    String[] array = fullName.split(" ");
    int length = array.length;
    if (length>0){
      this.setNameFirst(array[0]);
      int i = 1;
      while ( i < length -1) {
        this.setNameMiddle(this.getNameMiddle() +  (i == 1 ? "": " ") + array[i] );
        i++;
      }
      this.setNameLast(length>1 ? array[length-1] : "");
    }
  }

}