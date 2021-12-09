package org.immregistries.ehr.model;
import java.io.Serializable;
import java.util.Date;

public class Silo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int siloId = 0;
    private Tester tester = null;
    private String nameDisplay = "";

    public int getSiloId() {
        return siloId;
    }

    public void setSiloId(int siloId) {
        this.siloId = siloId;
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
