package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;

import java.util.Random;

public interface IIpsWriter {

    public static final String EMPTY_REASON_SYSTEM = "http://terminology.hl7.org/CodeSystem/list-empty-reason";


    public IBaseResource ipsBundle(EhrPatient ehrPatient, Facility facility);

    public static String entryUrl(Integer count) {

        return "resource:" + (count);
//        return "urn:" + UUID.randomUUID();
    }

    public static String entryUrl() {
        Random random = new Random();
        int id = random.nextInt();
        id = id < 0 ? -1 * id : id;
        return entryUrl(id);
    }
}
