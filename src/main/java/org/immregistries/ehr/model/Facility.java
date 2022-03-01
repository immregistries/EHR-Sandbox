package org.immregistries.ehr.model;
import java.io.Serializable;

public class Facility implements Serializable {

    private static final long serialVersionUID = 1L;

    private int facilityId = 0;
    private Tenant tenant = null;
    private Facility parentFacility = null;
    private String nameDisplay = "";

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
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

    public String getNameDisplay() {
        return nameDisplay;
    }

    public void setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay;
    }
}