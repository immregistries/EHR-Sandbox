package org.immregistries.ehr.api.controllers;

public class ControllerHelper {
    public static final String TENANTS_HEADER = "tenants";
    public static final String TENANT_PATH_HEADER = "/" + TENANTS_HEADER;
    public static final String TENANTS_PATH = TENANT_PATH_HEADER;
    public static final String TENANT_ID_SUFFIX = "/{tenantId}";
    public static final String TENANT_ID_PATH = TENANTS_PATH + TENANT_ID_SUFFIX;

    public static final String CLINICIANS_HEADER = "clinicians";
    public static final String CLINICIAN_PATH_HEADER = "/" + CLINICIANS_HEADER;
    public static final String CLINICIAN_PATH = TENANT_ID_PATH + CLINICIAN_PATH_HEADER;
    public static final String CLINICIAN_ID_SUFFIX = "/{clinicianId}";
    public static final String CLINICIAN_ID_PATH = CLINICIAN_PATH + CLINICIAN_ID_SUFFIX;

    public static final String FACILITIES_HEADER = "facilities";
    public static final String FACILITY_PATH_HEADER = "/" + FACILITIES_HEADER;
    public static final String FACILITIES_PATH = TENANT_ID_PATH + FACILITY_PATH_HEADER;
    public static final String FACILITY_ID_SUFFIX = "/{facilityId}";
    public static final String FACILITY_ID_PATH = FACILITIES_PATH + FACILITY_ID_SUFFIX;

    public static final String PATIENTS_HEADER = "patients";
    public static final String PATIENT_PATH_HEADER = "/" + PATIENTS_HEADER;
    public static final String PATIENTS_PATH = FACILITY_ID_PATH + PATIENT_PATH_HEADER;
    public static final String PATIENT_ID_SUFFIX = "/{patientId}";
    public static final String PATIENT_ID_PATH = PATIENTS_PATH + PATIENT_ID_SUFFIX;

    public static final String GROUPS_HEADER = "groups";
    public static final String GROUPS_PATH_HEADER = "/" + GROUPS_HEADER;
    public static final String GROUPS_PATH = FACILITY_ID_PATH + GROUPS_PATH_HEADER;
    public static final String GROUP_ID_SUFFIX = "/{groupId}";
    public static final String GROUPS_ID_PATH = GROUPS_PATH + GROUP_ID_SUFFIX;

    public static final String VACCINATIONS_HEADER = "vaccinations";
    public static final String VACCINATIONS_PATH_HEADER = "/" + VACCINATIONS_HEADER;
    public static final String VACCINATIONS_PATH = PATIENT_ID_PATH + VACCINATIONS_PATH_HEADER;
    public static final String VACCINATION_ID_SUFFIX = "/{vaccinationId}";
    public static final String VACCINATION_ID_PATH = VACCINATIONS_PATH + VACCINATION_ID_SUFFIX;

    public static final String REGISTRY_HEADER = "registry";
    public static final String REGISTRY_PATH_HEADER = "/" + REGISTRY_HEADER;
    public static final String REGISTRY_ID_SUFFIX = "/{registryId}";
    public static final String REGISTRY_COMPLETE_SUFFIX = REGISTRY_PATH_HEADER + REGISTRY_ID_SUFFIX;
    public static final String REGISTRY_PATH = TENANT_ID_PATH + REGISTRY_COMPLETE_SUFFIX;
}
