package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "clinician")
//@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Clinician.class)
public class Clinician extends EhrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "clinician_id", nullable = false)
//    @JdbcTypeCode(SqlTypes.INTEGER)
    private String id;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    @JsonIgnore
    private Tenant tenant;

    @Column(name = "name_last", nullable = false)
    private String nameLast = "";

    @Column(name = "name_middle")
    private String nameMiddle = "";

    @Column(name = "name_first", nullable = false)
    private String nameFirst = "";

    @Column(name = "name_Suffix", nullable = true)
    private String nameSuffix = "";

    @Column(name = "name_Prefix", nullable = true)
    private String namePrefix = "";

    @Column(name = "qualification", nullable = true)
    private String qualification = "";

    @OneToMany(mappedBy = "enteringClinician")
    @JsonIgnore
    private Set<VaccinationEvent> vaccinationEventsEntering = new LinkedHashSet<>();

    @OneToMany(mappedBy = "orderingClinician")
    @JsonIgnore
    private Set<VaccinationEvent> vaccinationEventsOrdering = new LinkedHashSet<>();

    @OneToMany(mappedBy = "administeringClinician")
    @JsonIgnore
    private Set<VaccinationEvent> vaccinationEvents = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "clinician_aphone", joinColumns = @JoinColumn(name = "clinician_id"))
    private Set<EhrPhoneNumber> aphones = new LinkedHashSet<>();

    @ElementCollection()
    @CollectionTable(name = "clinician_identifiers", joinColumns = @JoinColumn(name = "clinician_id"))
    private Set<EhrIdentifier> identifiers = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "clinician_phone", joinColumns = @JoinColumn(name = "clinician_id"))
    private Set<EhrPhoneNumber> phones = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "clinician_address", joinColumns = @JoinColumn(name = "clinician_id"))
    private Set<EhrAddress> addresses = new LinkedHashSet<>();

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Set<VaccinationEvent> getVaccinationEvents() {
        return vaccinationEvents;
    }

    public void setVaccinationEvents(Set<VaccinationEvent> vaccinationEvents) {
        this.vaccinationEvents = vaccinationEvents;
    }

    public Set<VaccinationEvent> getVaccinationEventsOrdering() {
        return vaccinationEventsOrdering;
    }

    public void setVaccinationEventsOrdering(Set<VaccinationEvent> vaccinationEventsOrdering) {
        this.vaccinationEventsOrdering = vaccinationEventsOrdering;
    }

    public Set<VaccinationEvent> getVaccinationEventsEntering() {
        return vaccinationEventsEntering;
    }

    public void setVaccinationEventsEntering(Set<VaccinationEvent> vaccinationEventsEntering) {
        this.vaccinationEventsEntering = vaccinationEventsEntering;
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

    public String getNameLast() {
        return nameLast;
    }

    public void setNameLast(String nameLast) {
        this.nameLast = nameLast;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public Set<EhrPhoneNumber> getPhones() {
        return phones;
    }

    public void setPhones(Set<EhrPhoneNumber> phones) {
        this.phones = phones;
    }

    public Set<EhrAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<EhrAddress> addresses) {
        this.addresses = addresses;
    }

    public Set<EhrIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<EhrIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    @JsonIgnore
    public void addIdentifier(EhrIdentifier identifier) {
        if (this.identifiers == null) {
            this.identifiers = new LinkedHashSet<>(3);
        }
        this.identifiers.add(identifier);
    }

    @JsonIgnore
    public EhrAddress addAddress() {
        EhrAddress ehrAddress = new EhrAddress();
        addAddress(ehrAddress);
        return ehrAddress;
    }

    @JsonIgnore
    public void addAddress(EhrAddress address) {
        if (this.addresses == null) {
            this.addresses = new LinkedHashSet<>(3);
        }
        this.addresses.add(address);
    }

    @JsonIgnore
    public EhrPhoneNumber addPhoneNumber() {
        EhrPhoneNumber phoneNumber = new EhrPhoneNumber();
        addPhoneNumber(phoneNumber);
        return phoneNumber;
    }

    @JsonIgnore
    public void addPhoneNumber(EhrPhoneNumber phoneNumber) {
        if (phones == null) {
            this.phones = new HashSet<>(1);
        }
        this.phones.add(phoneNumber);
    }


    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }
}