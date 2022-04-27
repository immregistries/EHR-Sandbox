package org.immregistries.ehr.entities;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "next_of_kin")
public class NextOfKin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "next_of_kin_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference("patient-nextOfKin")
    private Patient patient;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "name_last", length = 250)
    private String nameLast = "";

    @Column(name = "name_first", length = 250)
    private String nameFirst = "";

    @Column(name = "name_middle", length = 250)
    private String nameMiddle = "";

    @Column(name = "mother_maiden", length = 250)
    private String motherMaiden = "";

    @Column(name = "sex", length = 250)
    private String sex = "";

    @Column(name = "race", length = 250)
    private String race = "";

    @Column(name = "address_line1", length = 250)
    private String addressLine1 = "";

    @Column(name = "address_line2", length = 250)
    private String addressLine2 = "";

    @Column(name = "address_city", length = 250)
    private String addressCity = "";

    @Column(name = "address_state", length = 250)
    private String addressState = "";

    @Column(name = "address_zip", length = 250)
    private String addressZip = "";

    @Column(name = "address_country", length = 250)
    private String addressCountry = "";

    @Column(name = "address_county_parish", length = 250)
    private String addressCountyParish = "";

    @Column(name = "phone", length = 250)
    private String phone = "";

    @Column(name = "email", length = 250)
    private String email = "";

    @Column(name = "ethnicity", length = 250)
    private String ethnicity = "";

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressCountyParish() {
        return addressCountyParish;
    }

    public void setAddressCountyParish(String addressCountyParish) {
        this.addressCountyParish = addressCountyParish;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMotherMaiden() {
        return motherMaiden;
    }

    public void setMotherMaiden(String motherMaiden) {
        this.motherMaiden = motherMaiden;
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

    public String getNameLast() {
        return nameLast;
    }

    public void setNameLast(String nameLast) {
        this.nameLast = nameLast;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}