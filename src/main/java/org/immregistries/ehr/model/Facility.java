package org.immregistries.ehr.model
import java.io.Serializable;
import java.util.Date;

public class Facility implements Serializable {

    private static final long serialVersionUID = 1L;

    private int facilityId = 0;
    private Silo siloId = null;
    private Facility parentFacilityId = null;
    private String nameDisplay = "";
    private String location = "";

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public Silo getSiloId() {
        return siloId;
    }

    public void setSiloId(Silo siloId) {
        this.siloId = siloId;
    }

    public Facility getParentFacilityId() {
        return parentFacilityId;
    }

    public void setParentFacilityId(Facility parentFacilityId) {
        this.parentFacilityId = parentFacilityId;
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