package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.immregistries.ehr.api.entities.embedabbles.EhrRace;

import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;
import static org.immregistries.ehr.logic.mapping.IPatientMapper.MRN_TYPE_VALUE;

@Entity
@Table(name = "patient")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = EhrPatient.class)
@Audited(targetAuditMode = NOT_AUDITED)
public class EhrPatient extends EhrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    @Column(name = "name_last", length = 250)
    private String nameLast = "";
    @Column(name = "name_first", length = 250)
    private String nameFirst = "";
    @Column(name = "name_middle", length = 250)
    private String nameMiddle = "";
    @Column(name = "name_suffix", length = 250)
    private String nameSuffix = "";
    @Column(name = "mother_maiden", length = 250)
    private String motherMaiden = "";
    @Column(name = "sex", length = 250)
    private String sex = "";
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_race", joinColumns = @JoinColumn(name = "patient_id"))
    private Set<EhrRace> races = new HashSet<EhrRace>();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_address", joinColumns = @JoinColumn(name = "patient_id"))
    private Set<EhrAddress> addresses = new LinkedHashSet<>();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_phone", joinColumns = @JoinColumn(name = "patient_id"))
    private Set<EhrPhoneNumber> phones = new LinkedHashSet<>();
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
    @Column(name = "financial_status", length = 50)
    private String financialStatus = "";


    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH})
    @JoinColumn(name = "clinician_id")
    @Audited(targetAuditMode = NOT_AUDITED) //TODO maybe audit
    private Clinician generalPractitioner;


    @OneToMany(mappedBy = "patient")
    @JsonIgnore
    private Set<VaccinationEvent> vaccinationEvents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ehrPatient", cascade = {CascadeType.ALL, CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("patient_next_of_kin_relationship")
    @NotAudited
    private List<NextOfKinRelationship> nextOfKinRelationships = new ArrayList<>();
//    private List<NextOfKinRelationship> nextOfKinRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "patient")
    @NotAudited
    private Set<Feedback> feedbacks = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_identifiers", joinColumns = @JoinColumn(name = "patient_id"))
    private Set<EhrIdentifier> identifiers = new LinkedHashSet<>();

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
            return new HashSet<>(0);
        } else {
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

    public Set<VaccinationEvent> getVaccinationEvents() {
        return vaccinationEvents;
    }

    public void setVaccinationEvents(Set<VaccinationEvent> vaccinationEvents) {
        this.vaccinationEvents = vaccinationEvents;
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

    @JsonIgnore
    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<EhrIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<EhrIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    /**
     * Helping method to extract MRN from patient identifiers, according to the type criteria
     */
    @JsonIgnore
    @NotAudited
    @Transient
    public EhrIdentifier getMrnEhrIdentifier() {
        return identifiers.stream().filter((identifier) -> identifier.getType().equals(MRN_TYPE_VALUE)).findFirst().orElse(null);
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public Set<EhrPhoneNumber> getPhones() {
        return phones;
    }

    public void setPhones(Set<EhrPhoneNumber> phones) {
        this.phones = phones;
    }

    public EhrPhoneNumber addPhoneNumber() {
        EhrPhoneNumber phoneNumber = new EhrPhoneNumber();
        addPhoneNumber(phoneNumber);
        return phoneNumber;
    }

    public void addPhoneNumber(EhrPhoneNumber phoneNumber) {
        if (phones == null) {
            this.phones = new HashSet<>(1);
        }
        this.phones.add(phoneNumber);
    }

    public Set<EhrRace> getRaces() {
        return races;
    }

    public void setRaces(Set<EhrRace> races) {
        this.races = races;
    }

    public EhrRace addRace() {
        EhrRace race = new EhrRace();
        addRace(race);
        return race;
    }

    public void addRace(EhrRace race) {
        if (this.races == null) {
            this.races = new LinkedHashSet<>(3);
        }
        this.races.add(race);
    }

    public Set<EhrAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<EhrAddress> addresses) {
        this.addresses = addresses;
    }

    public EhrAddress addAddress() {
        EhrAddress ehrAddress = new EhrAddress();
        addAddress(ehrAddress);
        return ehrAddress;
    }

    public void addAddress(EhrAddress address) {
        if (this.addresses == null) {
            this.addresses = new LinkedHashSet<>(3);
        }
        this.addresses.add(address);
    }

    public List<NextOfKinRelationship> getNextOfKinRelationships() {
        return nextOfKinRelationships;
    }

    public void setNextOfKinRelationships(List<NextOfKinRelationship> nextOfKinRelationships) {
        this.nextOfKinRelationships = nextOfKinRelationships;
    }

    public NextOfKinRelationship addNextOfKinRelationship() {
        NextOfKinRelationship nextOfKinRelationship = new NextOfKinRelationship();
        addNextOfKinRelationship(nextOfKinRelationship);
        return nextOfKinRelationship;
    }


    public void addNextOfKinRelationship(NextOfKinRelationship nextOfKinRelationship) {
        if (this.nextOfKinRelationships == null) {
            this.nextOfKinRelationships = new ArrayList<>(3);
        }
        this.nextOfKinRelationships.add(nextOfKinRelationship);
    }

    public String getFinancialStatus() {
        return financialStatus;
    }

    public void setFinancialStatus(String financialStatus) {
        this.financialStatus = financialStatus;
    }


    public Clinician getGeneralPractitioner() {
        return generalPractitioner;
    }

    public void setGeneralPractitioner(Clinician generalPractitioner) {
        this.generalPractitioner = generalPractitioner;
    }
}