package org.immregistries.ehr.api.entities;

import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

import java.util.Set;

public interface EhrEntityWithIdentifiers {

    Set<EhrIdentifier> getIdentifiers();

    void setIdentifiers(Set<EhrIdentifier> identifiers);
}
