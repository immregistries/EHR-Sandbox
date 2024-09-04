package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;

import java.util.Random;

public interface IIpsWriter {

    public IBaseResource ipsBundle(EhrPatient ehrPatient, Facility facility);

    public static String entryUrl(Integer count) {
        return "resource:" + (count);
    }

    public static String entryUrl() {
        Random random = new Random();
        int id = random.nextInt();
        id = id < 0 ? -1 * id : id;
        return "resource:" + id;
    }
}
