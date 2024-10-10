package org.immregistries.ehr.api.security;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

/**
 * Filter checking for user authorization on each request
 */
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;
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


    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean authorized = false;
        try {
            String jwt = parseJwt(request);
            if (request.getServletPath().split("/").length == 2 &&
                    (request.getServletPath().endsWith(".js") || request.getServletPath().endsWith(".ico") || request.getServletPath().endsWith(".css"))) {
                authorized = true;
            } else if (request.getServletPath().startsWith("/code_maps") || request.getServletPath().startsWith("/auth") || request.getServletPath().startsWith("/$create") || request.getServletPath().startsWith("/fhir") || request.getServletPath().startsWith("/healthy") || request.getServletPath().startsWith("/assets") || request.getServletPath().startsWith("/styles") || request.getServletPath().equals("/")) { // for registration no authorization needed
                authorized = true;
            } else if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Checking authorization if path matches "/tenants/{tenantId}/**"
                authorized = filterUrl(request);
            } // TODO figure why http security configuration does not skip filter
        } catch (Exception e) {
            logger.error("Cannot set user authentication: ", e);
        }
        if (authorized) {
            filterChain.doFilter(request, response);
        } else {
            logger.error("Unauthorized request path: '{}'", request.getServletPath());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    /**
     * Currently not used, for future functionality
     *
     * @param request
     * @return
     */
    private User parseBasic(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Basic ")) {
            String base64 = headerAuth.substring("Basic ".length());
            String base64decoded = new String(Base64.decodeBase64(base64));
            String[] parts = base64decoded.split(":");
            User user = new User();
            user.setUsername(parts[0]);
            user.setPassword(parts[1]);
            return user;
        }
        return null;
    }

    /**
     * TODO improve by reversing the logic
     *
     * @param request
     * @return
     * @throws InvalidRequestException
     */
    private boolean filterUrl(HttpServletRequest request) throws InvalidRequestException {
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
                    ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(registryId, userDetailsService.currentUserId())
                            .orElseThrow(() -> new InvalidRequestException("invalid registry id"));
                }
            }
            if (item.equals(TENANT_HEADER)) {
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    checkIfPotentialValidId(tenantId);
                    Tenant tenant = tenantRepository.findByIdAndUserId(tenantId, userDetailsService.currentUserId())
                            .orElseThrow(() -> new InvalidRequestException("invalid tenant id"));
                    request.setAttribute("TENANT_ID", tenant.getId());
                    request.setAttribute("TENANT_NAME", tenant.getNameDisplay());
                }
            }
            if (item.equals(FACILITY_HEADER)) {
                if (scanner.hasNextInt()) {
                    facilityId = scanner.nextInt();
                    checkIfPotentialValidId(facilityId);
                    Facility facility = facilityRepository.findByUserAndId(userDetailsService.currentUser(), facilityId)
                            .orElseThrow(() -> new InvalidRequestException("invalid facility id"));
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
                            .orElseThrow(() -> new InvalidRequestException("invalid facility id"));
                }
            } else if (item.equals(CLINICIAN_HEADER)) {
                if (scanner.hasNextInt()) {
                    clinicianId = scanner.nextInt();
                    checkIfPotentialValidId(clinicianId);
                    Clinician clinician = clinicianRepository.findByTenantIdAndId(tenantId, clinicianId)
                            .orElseThrow(() -> new InvalidRequestException("invalid clinician id"));
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
                            .orElseThrow(() -> new InvalidRequestException("invalid patient id"));
                }
            }
            if (item.equals(VACCINATION_HEADER)) {
                if (scanner.hasNextInt()) {
                    vaccinationId = scanner.nextInt();
                    checkIfPotentialValidId(vaccinationId);
                    VaccinationEvent vaccinationEvent = vaccinationEventRepository.findByAdministeringFacilityIdAndId(facilityId, vaccinationId)
                            .orElseThrow(() -> new InvalidRequestException("invalid vaccination id"));
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
                                .orElseThrow(() -> new InvalidRequestException("invalid group id"));
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
                            .orElseThrow(() -> new InvalidRequestException("invalid vaccination id"));
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
    private void checkIfPotentialValidId(String id) throws InvalidRequestException {
        try {
            if (Integer.parseInt(id) == -1) {
                throw new InvalidRequestException("Invalid id in path : -1");
            }
        } catch (NumberFormatException numberFormatException) {

        }


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
                throw new InvalidRequestException("Invalid id in path : -1");
            }
        } catch (NumberFormatException numberFormatException) {

        }
    }
}