package org.immregistries.ehr.api.security;
import java.io.IOException;
import java.util.Scanner;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean authorized = false;
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Checking authorization if path matches "/tenants/{tenantId}/**"
                authorized = isAuthorizedURI(request.getServletPath());

            } else { // for registration no authorization needed
                authorized = true;
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        if (authorized) {
//            logger.error("Authorized request");
            filterChain.doFilter(request, response);
        } else {
            logger.error("Unauthorized request");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        }
    }
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }

    /**
     * TODO improve by reversing the logic
     * @param url
     * @return
     * @throws IOException
     */
    private boolean isAuthorizedURI(String url) throws IOException {
//        logger.info("{}", url);
        int tenantId = -1;
        int facilityId = -1;
        String patientId = null;
        String vaccinationId = null;
        // Parsing the URI
        String[] split = url.split("/");
        int len = split.length;
        Scanner scanner = new Scanner(url).useDelimiter("/");
        String item = "";
        if(scanner.hasNext()) {
            item = scanner.next();
//            if (item.equals("ehr-sandbox") ) {
//                if (scanner.hasNext()) {
//                    item = scanner.next();
//                } else {
//                    return true;
//                }
//            }
            /**
             * Fhir Server
             */
            if (item.equals("fhir")) {
                return true;
            }
            /**
             * Fhir client
             */
            if (item.equals("imm-registry")) {
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    if (!immunizationRegistryRepository.existsByIdAndUserId(tenantId, userDetailsService.currentUserId())){
                        return  false;
                    }
                }
            }
            if (item.equals("tenants") ) {
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    if (!tenantRepository.existsByIdAndUserId(tenantId, userDetailsService.currentUserId())){
                        return  false;
                    }
                }
            }
            if (item.equals("facilities") ) {
                if (scanner.hasNextInt()) {
                    facilityId = scanner.nextInt();
                    if (!facilityRepository.existsByUserAndId(userDetailsService.currentUser(), facilityId)){
                        return  false;
                    }
                }
            }
        }


        if(scanner.hasNext()) {
            item = scanner.next();
            if (item.equals("facilities") ) {
                if (scanner.hasNextInt()) {
                    facilityId = scanner.nextInt();
                    if (!facilityRepository.existsByTenantIdAndId(tenantId,facilityId)){
                        return  false;
                    }
                }
            }else if (item.equals("clinicians") ) {
                if (scanner.hasNextInt()) {
                    facilityId = scanner.nextInt();
                    if (!clinicianRepository.existsByTenantIdAndId(tenantId,facilityId)){
                        return false;
                    }
                }
            }
        }


        if(scanner.hasNext()) {
            item = scanner.next();
            if (item.equals("patients") ) {
                if (scanner.hasNextInt()) {
                    patientId = scanner.next();
                    if (!patientId.contains("$") && !patientRepository.existsByFacilityIdAndId(facilityId,patientId)){
                        return  false;
                    }
                }
            }
            if (item.equals("vaccinations") ) {
                if (scanner.hasNextInt()) {
                    vaccinationId = scanner.next();
                    if (!vaccinationId.contains("$") &&!vaccinationEventRepository.existsByAdministeringFacilityIdAndId(facilityId,vaccinationId)){
                        return  false;
                    }
                }
            }
        }


        if(scanner.hasNext()) {
            item = scanner.next();
            if (item.equals("vaccinations") ) {
                if (scanner.hasNextInt()) {
                    vaccinationId = scanner.next();
                    if (!vaccinationId.contains("$") && !vaccinationEventRepository.existsByPatientIdAndId(patientId,vaccinationId)){
                        return  false;
                    }
                }
            }
        }
        return true;
    }
}