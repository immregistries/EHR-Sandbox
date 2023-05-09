package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tenant")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Tenant.class)
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id", nullable = false)
    private Integer id;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "name_display", nullable = false, length = 250)
    private String nameDisplay;

    @OneToMany(mappedBy = "tenant")
//    @JsonManagedReference("tenant-facility")
    @JsonIgnore()
    private Set<Facility> facilities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tenant")
//    @JsonManagedReference("tenant-facility")
    @JsonIgnore()
    private Set<Clinician> clinicians = new LinkedHashSet<>();

    public Tenant() {
    }

    public Tenant(Integer id, User user, String nameDisplay, Set<Facility> facilities, Set<Clinician> clinicians) {
        this.id = id;
        this.user = user;
        this.nameDisplay = nameDisplay;
        this.facilities = facilities;
        this.clinicians = clinicians;
    }


    public Set<Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(Set<Facility> facilities) {
        this.facilities = facilities;
    }

    public String getNameDisplay() {
        return nameDisplay;
    }

    public void setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<Clinician> getClinicians() {
        return clinicians;
    }

    public void setClinicians(Set<Clinician> clinicians) {
        this.clinicians = clinicians;
    }
}