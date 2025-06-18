package org.immregistries.ehr.logic.mapping;


import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Extension;

import java.text.SimpleDateFormat;

public class MappingHelper {

    public static final String MALE_SEX = "M";
    public static final String FEMALE_SEX = "F";
    public static final String UNKNOWN_SEX = "U";
    public static final String OTHER_SEX = "O";

    public static final String PATIENT = "Patient";
    public static final String IMMUNIZATION = "Immunization";
    public static final String OBSERVATION = "Observation";
    public static final String ORGANIZATION = "Organization";
    public static final String LOCATION = "Location";
    public static final String PERSON = "Person";
    public static final String PRACTITIONER = "Practitioner";

    public static final SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");


    public static final String PHONE_USE_V2_SYSTEM = "http://terminology.hl7.org/ValueSet/v2-0201";
    public static final String USE_EXTENSION_URL = "use";


    public static Coding extensionGetCoding(Extension extension) {
        return extension.getValueCoding();
    }

    /**
     * imitating org.hl7.fhir.r4.model.Extension.getValueCoding()
     *
     * @param extension R4 Extension
     * @return Value coding or empty value
     */
    public static org.hl7.fhir.r4.model.Coding extensionGetCoding(org.hl7.fhir.r4.model.Extension extension) {
        if (extension.getValue() == null) {
            return new org.hl7.fhir.r4.model.Coding();
        } else {
            if (!(extension.getValue() instanceof org.hl7.fhir.r4.model.Coding))
                throw new FHIRException("Type mismatch: the type Coding was expected, but " + extension.getValue().getClass().getName() + " was encountered");
            return extension.castToCoding(extension.getValue());
        }
    }


}
