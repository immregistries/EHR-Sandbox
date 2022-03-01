package org.immregistries.ehr.model;
import com.github.javafaker.Faker;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.iis.kernal.model.CodeMapManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    private int patientId = 0;
    private Facility facility = null;
    private Tenant tenant = null;
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

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
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

    public static Patient random(Tenant tenant, Facility facility){
        Faker faker = new Faker();

        int randDay = (int) (Math.random()*30+1);
        int randMonth = (int) (Math.random()*11);
        int randYear = (int) (Math.random()*121+1900);

        Random rand = new Random();

        long aDay = TimeUnit.DAYS.toMillis(1);
        long now = new Date().getTime();
        Date twoYearsAgo = new Date(now - aDay * 365 * 2);

        Date eightyYearsAgo = new Date(now - aDay * 365 * 80);
        Date fourtyYearsAgo = new Date(now - aDay * 365 * 40);
        Date tenDaysAgo = new Date(now - aDay * 10);
        Date fourYearsAgo = new Date(now - aDay*365*4);

        CodeMap codeMap = CodeMapManager.getCodeMap();
        Collection<Code> codeListGuardian=codeMap.getCodesForTable(CodesetType.PERSON_RELATIONSHIP);

        Date birthDate = between(eightyYearsAgo,tenDaysAgo );
        Date deathDate = between(fourYearsAgo,tenDaysAgo );
        Date pubIndicDate = between(twoYearsAgo, tenDaysAgo);
        Date protecIndicDate = between(twoYearsAgo, tenDaysAgo);
        Date regIndicDate = between(twoYearsAgo, tenDaysAgo);
        Date regStatusDate = between(twoYearsAgo, tenDaysAgo);

        Patient patient = new Patient();
        patient.setTenant(tenant);
        patient.setFacility(facility);

        patient.setNameFirst(faker.name().firstName());
        patient.setNameLast(faker.name().lastName());
        patient.setNameMiddle(faker.name().firstName());

        patient.setBirthDate(birthDate);
        if(randMonth%2==0) {
            patient.setSex("F");
        }else {
            patient.setSex("M");
        }

        patient.setAddressLine1(faker.address().streetAddress());
        patient.setAddressCity(faker.address().city());
        patient.setAddressCountry(faker.address().country());
        patient.setAddressCountyParish("county");
        patient.setAddressState(faker.address().state());

        patient.setPhone(faker.phoneNumber().phoneNumber());
        patient.setEmail(patient.getNameFirst() + randDay +"@email.com");

        patient.setBirthFlag("");
        patient.setBirthOrder("");

        patient.setDeathFlag("");
        int randomDecision = rand.nextInt(100);
        if(randomDecision<30) {
            patient.setDeathDate(deathDate);
        }

        patient.setEthnicity("Indian");
        patient.setRace("Asian");

        patient.setGuardianFirst(faker.name().firstName());
        patient.setGuardianLast(faker.name().lastName());
        patient.setGuardianMiddle(faker.name().firstName());
        patient.setMotherMaiden(faker.name().lastName());
        int count = 0;
        for(Code code : codeListGuardian) {
            patient.setGuardianRelationship(code.getValue());
            count+=1;
            if(randDay==count) {
                break;
            }
        }

        patient.setProtectionIndicator("0");
        patient.setPublicityIndicator("0");
        patient.setRegistryStatusIndicator("0");
        patient.setProtectionIndicatorDate(protecIndicDate);
        patient.setPublicityIndicatorDate(pubIndicDate);
        patient.setRegistryStatusIndicatorDate(regIndicDate);

        patient.setUpdatedDate(new Date());
        return patient;
    }

    private static Date between(Date startInclusive, Date endExclusive) {
        long startMillis = startInclusive.getTime();
        long endMillis = endExclusive.getTime();
        long randomMillisSinceEpoch = ThreadLocalRandom
                .current()
                .nextLong(startMillis, endMillis);

        return new Date(randomMillisSinceEpoch);
    }
}