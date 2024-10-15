package org.immregistries.ehr.api;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.Set;

import static org.immregistries.ehr.api.AuditRevisionListener.TENANT_NAME;

public enum ProcessingFlavor {
    NO_DEPRECATED("NO_DEPRECATED", "Excludes deprecated fields from codesets in forms and tables"),
    LOTTERY("LOTTERY", "Use external API to verify Lot Number validity in forms"),
    LIGUAL("LIGUAL", "Sets all name types as legal"),
    BIZNESS("BIZNESS", "(Incoming) Uses local id in FHIR references instead of business identifier"),
    R5("R5", "Missing non-critical required fields", true),
    R4("R4", "(In progress, might break some functionalities) Use Fhir R4 version");

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
        return (String) request.getAttribute(TENANT_NAME);
    }

    public boolean isActive() {
        return StringUtils.defaultIfBlank(tenantName(), "").contains(key);
    }


}
