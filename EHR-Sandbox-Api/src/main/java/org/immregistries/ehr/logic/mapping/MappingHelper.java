package org.immregistries.ehr.logic.mapping;


import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.r5.model.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MappingHelper {

	public static final String PATIENT = "Patient";
	public static final String IMMUNIZATION = "Immunization";
	public static final String OBSERVATION = "Observation";
	public static final String ORGANISATION = "Organisation";
	public static final String LOCATION = "Location";
	public static final String PERSON = "Person";
	public static final String PRACTITIONER = "Practitioner";

	public static final SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");

	public static org.hl7.fhir.r4.model.CodeableConcept extensionGetCodeableConcept(org.hl7.fhir.r4.model.Extension extension) {
		if ( extension != null) {
			return extension.castToCodeableConcept(extension.getValue());
		} else return null;
	}

	public static org.hl7.fhir.r4.model.Coding extensionGetCoding(org.hl7.fhir.r4.model.Extension extension) {
		if ( extension != null) {
			return extension.castToCoding(extension.getValue());
		} else {
			return null;
		}
	}

}
