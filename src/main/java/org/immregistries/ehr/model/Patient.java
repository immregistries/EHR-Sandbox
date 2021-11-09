package org.immregistries.ehr.model
import java.io.Serializable;
import java.util.Date;

public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    private int patientId = 0;
    private Facility facilityId = null;
    private Silo siloId = null;
    private Date createdDate = null;
    private Date updatedDate = null;
    private Date birthDate = null;
    private String nameLast = "";
    private String nameFirst = "";
    private String nameMiddle = "";
    private String motherMaiden = "";
    private String sex = "";
    private String race = "";
    private String addressLine1 = "";
    private String addressLine2 = "";
    private String addressCity = "";
    private String addressState = "";
    private String addressZip = "";
    private String addressCountry = "";
    private String addressCountyParish = "";
    private String phone = "";
    private String email = "";
    private String ethnicity = "";
    private String birthFlag = "";
    private String birthOrder = "";
    private String deathFlag = "";
    private Date deathDate = null;
    private String publicityIndicator = "";
    private Date publicityIndicatorDate = null;
    private String protectionIndicator = "";
    private Date protectionIndicatorDate = null;
    private String registryStatusIndicator = "";
    private Date registryStatusIndicatorDate = null;
    private String guardianLast = "";
    private String guardianFirst = "";
    private String guardianMiddle = "";
    private String guardianRelationship = "";

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public Facility getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Facility facilityId) {
        this.facilityId = facilityId;
    }

    public Silo getSiloId() {
        return siloId;
    }

    public void setSiloId(Silo siloId) {
        this.siloId = siloId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getNameLast() {
        return nameLast;
    }

    public void setNameLast(String nameLast) {
        this.nameLast = nameLast;
    }

    public String getNameFirst() {
        return nameFirst;
    }

    public void setNameFirst(String nameFirst) {
        this.nameFirst = nameFirst;
    }

    public String getNameMiddle() {
        return nameMiddle;
    }

    public void setNameMiddle(String nameMiddle) {
        this.nameMiddle = nameMiddle;
    }

    public String getMotherMaiden() {
        return motherMaiden;
    }

    public void setMotherMaiden(String motherMaiden) {
        this.motherMaiden = motherMaiden;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressCountyParish() {
        return addressCountyParish;
    }

    public void setAddressCountyParish(String addressCountyParish) {
        this.addressCountyParish = addressCountyParish;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getBirthFlag() {
        return birthFlag;
    }

    public void setBirthFlag(String birthFlag) {
        this.birthFlag = birthFlag;
    }

    public String getBirthOrder() {
        return birthOrder;
    }

    public void setBirthOrder(String birthOrder) {
        this.birthOrder = birthOrder;
    }

    public String getDeathFlag() {
        return deathFlag;
    }

    public void setDeathFlag(String deathFlag) {
        this.deathFlag = deathFlag;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getPublicityIndicator() {
        return publicityIndicator;
    }

    public void setPublicityIndicator(String publicityIndicator) {
        this.publicityIndicator = publicityIndicator;
    }

    public Date getPublicityIndicatorDate() {
        return publicityIndicatorDate;
    }

    public void setPublicityIndicatorDate(Date publicityIndicatorDate) {
        this.publicityIndicatorDate = publicityIndicatorDate;
    }

    public String getProtectionIndicator() {
        return protectionIndicator;
    }

    public void setProtectionIndicator(String protectionIndicator) {
        this.protectionIndicator = protectionIndicator;
    }

    public Date getProtectionIndicatorDate() {
        return protectionIndicatorDate;
    }

    public void setProtectionIndicatorDate(Date protectionIndicatorDate) {
        this.protectionIndicatorDate = protectionIndicatorDate;
    }

    public String getRegistryStatusIndicator() {
        return registryStatusIndicator;
    }

    public void setRegistryStatusIndicator(String registryStatusIndicator) {
        this.registryStatusIndicator = registryStatusIndicator;
    }

    public Date getRegistryStatusIndicatorDate() {
        return registryStatusIndicatorDate;
    }

    public void setRegistryStatusIndicatorDate(Date registryStatusIndicatorDate) {
        this.registryStatusIndicatorDate = registryStatusIndicatorDate;
    }

    public String getGuardianLast() {
        return guardianLast;
    }

    public void setGuardianLast(String guardianLast) {
        this.guardianLast = guardianLast;
    }

    public String getGuardianFirst() {
        return guardianFirst;
    }

    public void setGuardianFirst(String guardianFirst) {
        this.guardianFirst = guardianFirst;
    }

    public String getGuardianMiddle() {
        return guardianMiddle;
    }

    public void setGuardianMiddle(String guardianMiddle) {
        this.guardianMiddle = guardianMiddle;
    }

    public String getGuardianRelationship() {
        return guardianRelationship;
    }

    public void setGuardianRelationship(String guardianRelationship) {
        this.guardianRelationship = guardianRelationship;
    }
}