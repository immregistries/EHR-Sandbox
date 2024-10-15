package org.immregistries.ehr.api.security;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

public class AuthorizationPathFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(AuthorizationPathFilter.class);
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean authorized = false;
        try {
            authorized = filterUrl(request);
        } catch (Exception e) {
            logger.error("Cannot set check authorization: ", e);
        }
        if (authorized) {
            filterChain.doFilter(request, response);
        } else {
            logger.error("Unauthorized request path: {}", request.getServletPath());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        }
    }

    /**
     * TODO improve by reversing the logic
     *
     * @param request
     * @return
     * @throws InvalidRequestException
     */
    private boolean filterUrl(HttpServletRequest request) throws InvalidRequestException {
        Integer userId = userDetailsService.currentUserId();

        String url = request.getServletPath();
//        logger.info("{}", url);
        Integer tenantId = null;
        Integer facilityId = null;
        Integer clinicianId = null;
        Integer registryId = null;
        Integer patientId = null;
        Integer vaccinationId = null;
        Integer groupId = null;
        // Parsing the URI
        String[] split = url.split("/");
        int len = split.length;
        Scanner scanner = new Scanner(url).useDelimiter("/");
        String item = "";
        if (scanner.hasNext()) {
            item = scanner.next();
            /**
             * Fhir Server
             */
            if (item.equals("fhir")) {
                return true;
            }
            /**
             * Fhir client
             */
            if (item.equals(REGISTRY_HEADER)) {
                if (scanner.hasNextInt()) {
                    registryId = scanner.nextInt();
//                    checkIfPotentialValidId(vaccinationId);
                    ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(registryId, userId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid registry id"));
                }
            }
            if (item.equals(TENANT_HEADER)) {
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    checkIfPotentialValidId(tenantId);
                    Tenant tenant = tenantRepository.findByIdAndUserId(tenantId, userId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid tenant id"));
                    request.setAttribute("TENANT_ID", tenant.getId());
                    request.setAttribute("TENANT_NAME", tenant.getNameDisplay());
                }
            }
            if (item.equals(FACILITY_HEADER)) {
                if (scanner.hasNextInt()) {
                    facilityId = scanner.nextInt();
                    checkIfPotentialValidId(facilityId);
                    Facility facility = facilityRepository.findByUserAndId(userDetailsService.currentUser(), facilityId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid facility id"));
                }
            }
        }


        if (scanner.hasNext()) {
            item = scanner.next();
            if (item.equals(FACILITY_HEADER)) {
                if (scanner.hasNextInt()) {
                    facilityId = scanner.nextInt();
                    checkIfPotentialValidId(facilityId);
                    Facility facility = facilityRepository.findByIdAndTenantId(facilityId, tenantId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid facility id"));
                }
            } else if (item.equals(CLINICIAN_HEADER)) {
                if (scanner.hasNextInt()) {
                    clinicianId = scanner.nextInt();
                    checkIfPotentialValidId(clinicianId);
                    Clinician clinician = clinicianRepository.findByTenantIdAndId(tenantId, clinicianId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid clinician id"));
                }
            }
        }


        if (scanner.hasNext()) {
            item = scanner.next();
            if (item.equals(PATIENT_HEADER)) {
                if (scanner.hasNextInt()) {
                    patientId = scanner.nextInt();
                    checkIfPotentialValidId(patientId);
                    EhrPatient ehrPatient = patientRepository.findByFacilityIdAndId(facilityId, patientId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid patient id"));
                }
            }
            if (item.equals(VACCINATION_HEADER)) {
                if (scanner.hasNextInt()) {
                    vaccinationId = scanner.nextInt();
                    checkIfPotentialValidId(vaccinationId);
                    VaccinationEvent vaccinationEvent = vaccinationEventRepository.findByAdministeringFacilityIdAndId(facilityId, vaccinationId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid vaccination id"));
                }
            }
            if (item.equals(GROUP_HEADER)) {
                if (scanner.hasNextInt()) {
                    groupId = scanner.nextInt();
                    checkIfPotentialValidId(groupId);
                    EhrGroup ehrGroup;
                    Optional<EhrGroup> ehrGroupOptional = ehrGroupRepository.findByFacilityIdAndId(facilityId, groupId);
                    if (ehrGroupOptional.isPresent()) {
                        ehrGroup = ehrGroupOptional.get();
                    } else {
                        /**
                         * Groups related to children facilities are accessible , TODO improve consistency
                         */
                        ehrGroup = ehrGroupRepository.findByTenantIdAndId(tenantId, groupId)
                                .orElseThrow(() -> new InvalidRequestException("Invalid group id"));
                    }
                }
            }
        }


        if (scanner.hasNext()) {
            item = scanner.next();
            if (item.equals(VACCINATION_HEADER)) {
                if (scanner.hasNextInt()) {
                    vaccinationId = scanner.nextInt();
                    VaccinationEvent vaccinationEvent = vaccinationEventRepository.findByPatientIdAndId(patientId, vaccinationId)
                            .orElseThrow(() -> new InvalidRequestException("Invalid vaccination id"));
                }
            }
        }
        return true;
    }

    /**
     * Used to avoid unauthorized exceptions when id are -1
     *
     * @param id
     * @throws InvalidRequestException
     */
    private void checkIfPotentialValidId(Integer id) throws InvalidRequestException {
        try {
            if (id == -1) {
                throw new InvalidRequestException("Invalid id as path variable: -1");
            }
        } catch (NumberFormatException numberFormatException) {

        }
    }

}
