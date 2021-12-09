package org.immregistries.ehr.model;
import java.io.Serializable;
import java.util.Date;

public class Facility implements Serializable {

    private static final long serialVersionUID = 1L;

    private int facilityId = 0;
    private Silo silo = null;
    private Facility parentFacility = null;
    private String nameDisplay = "";
    private String location = "";

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public Silo getSilo() {
        return silo;
    }

    public void setSilo(Silo silo) {
        this.silo = silo;
    }

    public Facility getParentFacility() {
        return parentFacility;
    }

    public void setParentFacility(Facility parentFacility) {
        this.parentFacility = parentFacility;
    }

    public String getNameDisplay() {
        return nameDisplay;
    }

    public void setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}