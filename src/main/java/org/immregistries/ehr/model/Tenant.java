package org.immregistries.ehr.model;
import java.io.Serializable;

public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tenantId = 0;
    private User user = null;
    private String nameDisplay = "";

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNameDisplay() {
        return nameDisplay;
    }

    public void setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay;
    }
}
