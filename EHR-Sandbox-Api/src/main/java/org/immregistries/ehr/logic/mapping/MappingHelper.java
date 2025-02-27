package org.immregistries.ehr.logic.mapping;


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


}
