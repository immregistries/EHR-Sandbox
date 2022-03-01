package org.immregistries.ehr.model;
import java.io.Serializable;

public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tenantId = 0;
    private Tester tester = null;
    private String nameDisplay = "";

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Tester getTester() {
        return tester;
    }

    public void setTester(Tester tester) {
        this.tester = tester;
    }

    public String getNameDisplay() {
        return nameDisplay;
    }

    public void setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay;
    }
}
