package org.immregistries.ehr.api.entities;

public class EhrUtils {
    public static String convert(Integer value) {
        if (value == null) {
            return null;
        } else {
            return String.valueOf(value);
        }
    }

    public static Integer convert(String value) {
        if (value == null) {
            return null;
        } else {
            return Integer.parseInt(value);
        }
    }
}
