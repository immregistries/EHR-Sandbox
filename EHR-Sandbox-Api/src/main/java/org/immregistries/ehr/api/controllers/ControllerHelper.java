package org.immregistries.ehr.api.controllers;

public class ControllerHelper {
    public static final String TENANT_HEADER = "tenants";
    public static final String TENANT_PATH_HEADER = "/" + TENANT_HEADER;
    public static final String TENANT_PATH = TENANT_PATH_HEADER;
    public static final String TENANT_ID_SUFFIX = "/{tenantId}";
    public static final String TENANT_ID_PATH = TENANT_PATH + TENANT_ID_SUFFIX;

    public static final String CLINICIAN_HEADER = "clinicians";
    public static final String CLINICIAN_PATH_HEADER = "/" + CLINICIAN_HEADER;
    public static final String CLINICIAN_PATH = TENANT_ID_PATH + CLINICIAN_PATH_HEADER;
    public static final String CLINICIAN_ID_SUFFIX = "/{clinicianId}";
    public static final String CLINICIAN_ID_PATH = CLINICIAN_PATH + CLINICIAN_ID_SUFFIX;

    public static final String FACILITY_HEADER = "facilities";
    public static final String FACILITY_PATH_HEADER = "/" + FACILITY_HEADER;
    public static final String FACILITY_PATH = TENANT_ID_PATH + FACILITY_PATH_HEADER;
    public static final String FACILITY_ID_SUFFIX = "/{facilityId}";
    public static final String FACILITY_ID_PATH = FACILITY_PATH + FACILITY_ID_SUFFIX;

    public static final String PATIENT_HEADER = "patients";
    public static final String PATIENT_PATH_HEADER = "/" + PATIENT_HEADER;
    public static final String PATIENT_PATH = FACILITY_ID_PATH + PATIENT_PATH_HEADER;
    public static final String PATIENT_ID_SUFFIX = "/{patientId}";
    public static final String PATIENT_ID_PATH = PATIENT_PATH + PATIENT_ID_SUFFIX;

    public static final String GROUP_HEADER = "groups";
    public static final String GROUP_PATH_HEADER = "/" + GROUP_HEADER;
    public static final String GROUP_PATH = FACILITY_ID_PATH + GROUP_PATH_HEADER;
    public static final String GROUP_ID_SUFFIX = "/{groupId}";
    public static final String GROUP_ID_PATH = GROUP_PATH + GROUP_ID_SUFFIX;

    public static final String VACCINATION_HEADER = "vaccinations";
    public static final String VACCINATION_PATH_HEADER = "/" + VACCINATION_HEADER;
    public static final String VACCINATION_PATH = PATIENT_ID_PATH + VACCINATION_PATH_HEADER;
    public static final String VACCINATION_ID_SUFFIX = "/{vaccinationId}";
    public static final String VACCINATION_ID_PATH = VACCINATION_PATH + VACCINATION_ID_SUFFIX;

    public static final String REGISTRY_HEADER = "registry";
    public static final String REGISTRY_PATH_HEADER = "/" + REGISTRY_HEADER;
    public static final String REGISTRY_ID_SUFFIX = "/{registryId}";
    public static final String REGISTRY_COMPLETE_SUFFIX = REGISTRY_PATH_HEADER + REGISTRY_ID_SUFFIX;
    public static final String REGISTRY_PATH = TENANT_ID_PATH + REGISTRY_COMPLETE_SUFFIX;
}
