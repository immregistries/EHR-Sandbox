package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.envers.NotAudited;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * TODO integrate more in mapping and remove 'guardian' fields
 */
@Entity
@Table(name = "next_of_kin")
public class NextOfKin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "next_of_kin_id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference("patient-nextOfKin")
    private EhrPatient patient;

    @OneToMany(mappedBy = "nextOfKin", orphanRemoval = true)
//    @JsonManagedReference("patient-nextOfKin-relationship")
    @NotAudited
    @JsonIgnore()
    private Set<NextOfKinRelationship> nextOfKinRelationShips = new LinkedHashSet<>();

    @Column(name = "birth_date")
    private LocalDate birthDate;

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

    @Column(name = "race", length = 250)
    private String race = "";

    @ElementCollection()
    @CollectionTable(name = "next_of_kin_address", joinColumns = @JoinColumn(name = "next_of_kin_id"))
    private Set<EhrAddress> addresses = new LinkedHashSet<>();

    @Column(name = "email", length = 250)
    private String email = "";

    @Column(name = "ethnicity", length = 250)
    private String ethnicity = "";

    @ElementCollection()
    @CollectionTable(name = "next_of_kin_phone", joinColumns = @JoinColumn(name = "next_of_kin_id"))
    private Set<EhrPhoneNumber> phoneNumbers = new LinkedHashSet<>();

    public Set<EhrPhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(Set<EhrPhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void addPhoneNumber(EhrPhoneNumber phoneNumber) {
        if (this.phoneNumbers == null) {
            this.phoneNumbers = new HashSet<>(2);
        }
        this.phoneNumbers.add(phoneNumber);
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

    public EhrPatient getPatient() {
        return patient;
    }

    public void setPatient(EhrPatient patient) {
        this.patient = patient;
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

    public Set<NextOfKinRelationship> getNextOfKinRelationShips() {
        return nextOfKinRelationShips;
    }

    public void setNextOfKinRelationShips(Set<NextOfKinRelationship> nextOfKinRelationShips) {
        this.nextOfKinRelationShips = nextOfKinRelationShips;
    }

    public Set<EhrAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<EhrAddress> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(EhrAddress address) {
        if (this.addresses == null) {
            this.addresses = new LinkedHashSet<>(3);
        }
        this.addresses.add(address);
    }
}