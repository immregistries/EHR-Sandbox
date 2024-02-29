package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hl7.fhir.r5.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "patient", indexes = {
        @Index(name = "facility_id", columnList = "facility_id")
})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = EhrPatient.class)
@Audited(targetAuditMode = NOT_AUDITED)
//@Configurable(preConstruction = true)
public class EhrPatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id", nullable = false)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    @JsonBackReference("facility-patient")
    @Audited(targetAuditMode = NOT_AUDITED)
    private Facility facility;
    @Column(name = "created_date", nullable = false)
    private Date createdDate;
    @Column(name = "updated_date", nullable = false)
    private Date updatedDate;
    @Column(name = "birth_date", nullable = false)
    private Date birthDate;
    @Column(name = "mrn", length = 125)
    private String mrn = "";
    @Column(name = "mrn_system", length = 125)
    private String mrnSystem = "";
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
    @Column(name = "birth_flag", length = 1)
    private String birthFlag = "";
    @Column(name = "birth_order", length = 250)
    private String birthOrder = "";
    @Column(name = "death_flag", length = 1)
    private String deathFlag = "";
    @Column(name = "death_date")
    private Date deathDate;
    @Column(name = "publicity_indicator", length = 250)
    private String publicityIndicator = "";
    @Column(name = "publicity_indicator_date")
    private Date publicityIndicatorDate;
    @Column(name = "protection_indicator", length = 250)
    private String protectionIndicator = "";
    @Column(name = "protection_indicator_date")
    private Date protectionIndicatorDate;
    @Column(name = "registry_status_indicator", length = 250)
    private String registryStatusIndicator = "";
    @Column(name = "registry_status_indicator_date")
    private Date registryStatusIndicatorDate;
    @Column(name = "guardian_last", length = 250)
    private String guardianLast = "";
    @Column(name = "guardian_first", length = 250)
    private String guardianFirst = "";
    @Column(name = "guardian_middle", length = 250)
    private String guardianMiddle = "";
    @Column(name = "guardian_relationship", length = 250)
    private String guardianRelationship = "";
    @OneToMany(mappedBy = "patient")
    @JsonIgnore
    private Set<VaccinationEvent> vaccinationEvents = new LinkedHashSet<>();
    @OneToMany(mappedBy = "patient")
    @JsonManagedReference("patient-nextOfKin")
    @NotAudited
    private Set<NextOfKin> nextOfKins = new LinkedHashSet<>();
    @OneToMany(mappedBy = "patient")
    @NotAudited
//    @JsonDeserialize(using = CustomFeedbackListDeserializer.class)
    private Set<Feedback> feedbacks = new LinkedHashSet<>();

    @NotAudited
    @ManyToMany
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    @JsonIgnore
    private Set<EhrGroup> ehrGroups = new LinkedHashSet<>();

    @JsonInclude()
    @Transient
    public Set<String> getGroupNames() {
        if (ehrGroups.isEmpty()) {
            return  new HashSet<>(0);
        }
        else  {
            return this.ehrGroups.stream().map(EhrGroup::getName).collect(Collectors.toSet());
        }
    }

    public Set<EhrGroup> getEhrGroups() {
        return ehrGroups;
    }

    public void setEhrGroups(Set<EhrGroup> ehrGroups) {
        this.ehrGroups = ehrGroups;
    }

    public Set<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(Set<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Set<NextOfKin> getNextOfKins() {
        return nextOfKins;
    }

    public void setNextOfKins(Set<NextOfKin> nextOfKins) {
        this.nextOfKins = nextOfKins;
    }

    public Set<VaccinationEvent> getVaccinationEvents() {
        return vaccinationEvents;
    }

    public void setVaccinationEvents(Set<VaccinationEvent> vaccinationEvents) {
        this.vaccinationEvents = vaccinationEvents;
    }

    public String getGuardianRelationship() {
        return guardianRelationship;
    }

    public void setGuardianRelationship(String guardianRelationship) {
        this.guardianRelationship = guardianRelationship;
    }

    public String getGuardianMiddle() {
        return guardianMiddle;
    }

    public void setGuardianMiddle(String guardianMiddle) {
        this.guardianMiddle = guardianMiddle;
    }

    public String getGuardianFirst() {
        return guardianFirst;
    }

    public void setGuardianFirst(String guardianFirst) {
        this.guardianFirst = guardianFirst;
    }

    public String getGuardianLast() {
        return guardianLast;
    }

    public void setGuardianLast(String guardianLast) {
        this.guardianLast = guardianLast;
    }

    public Date getRegistryStatusIndicatorDate() {
        return registryStatusIndicatorDate;
    }

    public void setRegistryStatusIndicatorDate(Date registryStatusIndicatorDate) {
        this.registryStatusIndicatorDate = registryStatusIndicatorDate;
    }

    public String getRegistryStatusIndicator() {
        return registryStatusIndicator;
    }

    public void setRegistryStatusIndicator(String registryStatusIndicator) {
        this.registryStatusIndicator = registryStatusIndicator;
    }

    public Date getProtectionIndicatorDate() {
        return protectionIndicatorDate;
    }

    public void setProtectionIndicatorDate(Date protectionIndicatorDate) {
        this.protectionIndicatorDate = protectionIndicatorDate;
    }

    public String getProtectionIndicator() {
        return protectionIndicator;
    }

    public void setProtectionIndicator(String protectionIndicator) {
        this.protectionIndicator = protectionIndicator;
    }

    public Date getPublicityIndicatorDate() {
        return publicityIndicatorDate;
    }

    public void setPublicityIndicatorDate(Date publicityIndicatorDate) {
        this.publicityIndicatorDate = publicityIndicatorDate;
    }

    public String getPublicityIndicator() {
        return publicityIndicator;
    }

    public void setPublicityIndicator(String publicityIndicator) {
        this.publicityIndicator = publicityIndicator;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getDeathFlag() {
        return deathFlag;
    }

    public void setDeathFlag(String deathFlag) {
        this.deathFlag = deathFlag;
    }

    public String getBirthOrder() {
        return birthOrder;
    }

    public void setBirthOrder(String birthOrder) {
        this.birthOrder = birthOrder;
    }

    public String getBirthFlag() {
        return birthFlag;
    }

    public void setBirthFlag(String birthFlag) {
        this.birthFlag = birthFlag;
    }

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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public String getMrnSystem() {
        return mrnSystem;
    }

    public void setMrnSystem(String mrnSystem) {
        this.mrnSystem = mrnSystem;
    }


}