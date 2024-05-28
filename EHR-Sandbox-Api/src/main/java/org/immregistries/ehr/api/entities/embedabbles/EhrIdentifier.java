package org.immregistries.ehr.api.entities.embedabbles;

import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable()
public class EhrIdentifier {

    @Size(max = 300)
    private String system;

    @Size(max = 300)
    private String value;

    @Size(max = 300)
    private String type;


    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
