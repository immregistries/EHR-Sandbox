package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "clinician")
//@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler"})
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Clinician.class)
public class Clinician extends EhrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clinician_id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    @JsonIgnore
    private Tenant tenant;

    @Column(name = "name_last", nullable = false, length = 250)
    private String nameLast = "";

    @Column(name = "name_middle", length = 250)
    private String nameMiddle = "";

    @Column(name = "name_first", nullable = false, length = 250)
    private String nameFirst = "";

    @Column(name = "name_Suffix", nullable = true, length = 250)
    private String nameSuffix = "";

    @Column(name = "name_Prefix", nullable = true, length = 250)
    private String namePrefix = "";

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    @Column(name = "qualification", nullable = true, length = 250)
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

    @ElementCollection()
    @CollectionTable(name = "clinician_identifiers", joinColumns = @JoinColumn(name = "clinician_id"))
    private Set<EhrIdentifier> identifiers = new LinkedHashSet<>();

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<EhrIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<EhrIdentifier> identifiers) {
        this.identifiers = identifiers;
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
}