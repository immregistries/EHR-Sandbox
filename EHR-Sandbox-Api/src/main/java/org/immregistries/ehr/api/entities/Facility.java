package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.JoinFormula;
import org.springframework.data.jpa.repository.EntityGraph;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "facility"
//        ,indexes = {@Index(name = "tenant_id", columnList = "tenant_id")}
)
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class,
        property="id",
        scope = Facility.class)
///**
// * Solves a "No Session exception" when using facility.getPatients(), issue about lazy loading apparently
// */
//@NamedEntityGraph(name = "Facility.patients",
//        attributeNodes = @NamedAttributeNode("patients")
//)
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
//    @JsonBackReference("tenant-facility")
//    @JsonIdentityReference()
    private Tenant tenant;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_facility_id")
    @JsonBackReference("parent")
    private Facility parentFacility;

    @Column(name = "name_display", nullable = false, length = 250)
    private String nameDisplay = "";

    @OneToMany(mappedBy = "administeringFacility")
    @JsonIgnore
    private Set<VaccinationEvent> vaccinationEvents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "parentFacility")
    @JsonManagedReference("parent")
    private Set<Facility> facilities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "facility")
//    @JsonManagedReference("facility-patient")
    @JsonIgnore
    private Set<EhrPatient> patients = new LinkedHashSet<>();

    @OneToMany(mappedBy = "facility")
//    @JsonManagedReference( value = "facility-feedback")
    @JsonIgnore
    private Set<Feedback> feedbacks = new LinkedHashSet<>();

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}