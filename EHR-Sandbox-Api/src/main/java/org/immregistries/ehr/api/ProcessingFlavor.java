package org.immregistries.ehr.api;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.Set;

import static org.immregistries.ehr.api.AuditRevisionListener.TENANT_NAME;

public enum ProcessingFlavor {
    BLACKJACK("BLACKJACK", "Generates fully random Records instead of Synthea when populating facilities"),
    PAIN_PERDU("PAINPERDU", "Includes fields marked as deprecated from codesets in forms and tables"),
    LOTTERY("LOTTERY", "Use external API to verify Lot Number validity in forms"),
    //    LIGUAL("LIGUAL", "Sets all name types as legal"),
//    BIZNESS("BIZNESS", "(Incoming) Uses local id in FHIR references instead of business identifier"),
    R5("R5", "Missing non-critical required fields", true),
    R4("R4", "(In progress, might break some functionalities) Use Fhir R4 version"),
    Z44("Z44", "Use Z44 profile for QBP instead of default Z34"),
    BABYNAME("BABYNAME", "Detect invalid names and change name type to 'Newborn' or 'TEST' when producing HL7v2 messages"),
    GLOTTOPHOBIA("GLOTTOPHOBIA", "Accents adn special characters like ñéë are converted to ASCII"),
    UPPERCASE("UPPERCASE", "Converts all names to uppercase before sending to demonstrate incompatibility with case sensitivity in some systems."),
    SINGLENAME("SINGLENAME", "Only allow single Name for patients in forms, Name type set to Legal by default"),
    LEGALFIRST("LEGALFIRST", "Legal name is first in segment for Hl7v2");

    private String key;
    private String description;
    private boolean hidden = false;

    ProcessingFlavor(String key, String description) {
        this.key = key;
        this.description = description;
    }

    ProcessingFlavor(String key, String description, boolean hidden) {
        this.key = key;
        this.description = description;
        this.hidden = hidden;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public static Set<ProcessingFlavor> getProcessingStyle(String label) {
        Set<ProcessingFlavor> processingFlavorSet = new HashSet<>();
        if (label != null) {
            label = label.toUpperCase();
            for (ProcessingFlavor ps : ProcessingFlavor.values()) {
                String key = ps.key.toUpperCase();
                if (label.startsWith(key + " ")
                        || label.endsWith(" " + key)
                        || label.indexOf(" " + key + " ") > 0) {
                    processingFlavorSet.add(ps);
                } else if (label.startsWith(key + "_")
                        || label.endsWith("_" + key)
                        || label.indexOf("_" + key + "_") > 0) {
                    processingFlavorSet.add(ps);
                } else if (label.equals(key)) {
                    processingFlavorSet.add(ps);
                }
            }
        }
        return processingFlavorSet;
    }

    public static Set<ProcessingFlavor> getCurrentProcessingStyle() {
        return getProcessingStyle(tenantName());
    }

    private static String tenantName() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return StringUtils.defaultIfBlank((String) request.getAttribute(TENANT_NAME), "");
    }

    public boolean isActive() {
        String label = tenantName();
        if (StringUtils.isBlank(tenantName())) {
            return false;
        } else if (label.startsWith(key + " ")
                || label.endsWith(" " + key)
                || label.indexOf(" " + key + " ") > 0) {
            return true;
        } else if (label.startsWith(key + "_")
                || label.endsWith("_" + key)
                || label.indexOf("_" + key + "_") > 0) {
            return true;
        } else if (label.equals(key)) {
            return true;
        }
        return false;
    }


}
