package org.immregistries.ehr.api.entities.embedabbles;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable()
public class EhrRace {

    @Column(name = "race_value")
    private String value;

    public EhrRace() {
    }

    public EhrRace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
