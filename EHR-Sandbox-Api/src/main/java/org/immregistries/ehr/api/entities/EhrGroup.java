package org.immregistries.ehr.api.entities;

import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "group")
public class EhrGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "group_ id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    @JsonIgnore
    private Facility facility;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToMany()
    @JoinTable(name = "group_members", joinColumns = @JoinColumn(name = "group_ id"), inverseJoinColumns = @JoinColumn(name = "patient_id"))
    Set<EhrPatient> patientList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<EhrPatient> getPatientList() {
        return patientList;
    }

    public void setPatientList(Set<EhrPatient> patientList) {
        this.patientList = patientList;
    }
}