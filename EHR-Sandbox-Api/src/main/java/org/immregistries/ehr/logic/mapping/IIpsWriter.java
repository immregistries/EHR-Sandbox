package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;

public interface IIpsWriter {

    public IBaseResource ipsBundle(EhrPatient ehrPatient, Facility facility);
}
