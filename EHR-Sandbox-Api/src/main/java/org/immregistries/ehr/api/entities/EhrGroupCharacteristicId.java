package org.immregistries.ehr.api.entities;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class EhrGroupCharacteristicId implements Serializable {
    private String groupId;

    private String codeValue;

    private String codeSystem;

    public EhrGroupCharacteristicId() {
    }

    public EhrGroupCharacteristicId(EhrGroup ehrGroup) {
        this.groupId = String.valueOf(ehrGroup.getId());
    }

    public EhrGroupCharacteristicId(String groupId) {
        this.groupId = groupId;
    }

    public EhrGroupCharacteristicId(String groupId, String codeValue, String codeSystem) {
        this.groupId = groupId;
        this.codeValue = codeValue;
        this.codeSystem = codeSystem;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }
}