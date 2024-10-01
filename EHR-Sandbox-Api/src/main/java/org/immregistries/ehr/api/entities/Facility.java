package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "facility", indexes = {
        @Index(name = "idx_facility_id", columnList = "facility_id"),
        @Index(name = "idx_facility", columnList = "parent_facility_id")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Facility.class)
///**
// * Solves a "No Session exception" when using facility.getPatients(), issue about lazy loading apparently
// */
public class Facility extends EhrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "facility_id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
//    @JsonIgnore()
    @JsonIdentityReference()
    private Tenant tenant;

    @Column(name = "name_display", nullable = false, length = 250)
    private String nameDisplay = "";

    @Column(name = "facility_type", nullable = false, length = 250)
    private String type = "";

    @OneToMany(mappedBy = "administeringFacility")
    @JsonIgnore()
    private Set<VaccinationEvent> vaccinationEvents = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_facility_id")
//    @JsonBackReference("parent")
    @JsonIdentityReference(alwaysAsId = true)
    private Facility parentFacility;

    @OneToMany(mappedBy = "parentFacility", cascade = CascadeType.DETACH)
//    @JsonManagedReference("parent")
//    @JsonIgnore()
//    @JsonIdentityReference(alwaysAsId = true)
    private Set<Facility> facilities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "facility")
//    @JsonManagedReference("facility-patient")
    @JsonIgnore
    private Set<EhrPatient> patients = new LinkedHashSet<>();

    @OneToMany(mappedBy = "facility")
//    @JsonManagedReference( value = "facility-feedback")
    @JsonIgnore
    private Set<Feedback> feedbacks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "facility")
//    @JsonManagedReference( value = "facility-feedback")
    @JsonIgnore
    private Set<EhrGroup> groups = new LinkedHashSet<>();

    @ElementCollection()
    @CollectionTable(name = "facility_identifiers", joinColumns = @JoinColumn(name = "facility_id"))
    private Set<EhrIdentifier> identifiers = new LinkedHashSet<>();

    @JsonInclude()
    @Transient
    public Integer getChildrenCount() {
        return facilities.size();
    }

    @ElementCollection()
    @CollectionTable(name = "facility_addresses", joinColumns = @JoinColumn(name = "facility_id"))
    private Set<EhrAddress> addresses = new LinkedHashSet<>();

    public Set<EhrGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<EhrGroup> groups) {
        this.groups = groups;
    }

    public Set<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(Set<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Set<EhrPatient> getPatients() {
        return patients;
    }

    public void setPatients(Set<EhrPatient> patients) {
        this.patients = patients;
    }

    public Set<Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(Set<Facility> facilities) {
        this.facilities = facilities;
    }

    public Set<VaccinationEvent> getVaccinationEvents() {
        return vaccinationEvents;
    }

    public void setVaccinationEvents(Set<VaccinationEvent> vaccinationEvents) {
        this.vaccinationEvents = vaccinationEvents;
    }

    public String getNameDisplay() {
        return nameDisplay;
    }

    public void setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Facility getParentFacility() {
        return parentFacility;
    }

    public void setParentFacility(Facility parentFacility) {
        this.parentFacility = parentFacility;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<EhrAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<EhrAddress> addresses) {
        this.addresses = addresses;
    }
}