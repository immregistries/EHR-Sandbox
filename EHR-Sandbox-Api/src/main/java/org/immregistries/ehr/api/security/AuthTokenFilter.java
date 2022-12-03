package org.immregistries.ehr.api.security;
import java.io.IOException;
import java.util.Scanner;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.PatientRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;

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

    private boolean isAuthorizedURI(String url) throws IOException {
        int tenantId = -1;
        int facilityId = -1;
        int patientId = -1;
        int vaccinationId = -1;
        // Parsing the URI
        Scanner scanner = new Scanner(url).useDelimiter("/");
        String item = "";
        if(scanner.hasNext() && tenantId == -1) {
            item = scanner.next();
            if (item.equals("ehr-sandbox") ) {
                if (scanner.hasNext()) {
                    item = scanner.next();
                } else {
                    return true;
                }
            }
            if (item.equals("fhir") ) {
                return true;
            }
            if (item.equals("tenants") ) {
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    if (!tenantRepository.existsByIdAndUserId(tenantId, userDetailsService.currentUserId())){
                        return  false;
                    }
                }
            }
        }
        if(scanner.hasNext() && facilityId == -1 ) {
            item = scanner.next();
            if (item.equals("facilities") ) {
                if (scanner.hasNextInt()) {
                    facilityId = scanner.nextInt();
                    if (!facilityRepository.existsByTenantIdAndId(tenantId,facilityId)){
                        return  false;
                    }
                }
            }
        }
        if(scanner.hasNext() && patientId == -1 && vaccinationId == -1 ) {
            item = scanner.next();
            if (item.equals("patients") ) {
                if (scanner.hasNextInt()) {
                    patientId = scanner.nextInt();
                    if (!patientRepository.existsByFacilityIdAndId(facilityId,patientId)){
                        return  false;
                    }
                }
            }
            if (item.equals("vaccinations") ) {
                if (scanner.hasNextInt()) {
                    vaccinationId = scanner.nextInt();
                    if (!vaccinationEventRepository.existsByAdministeringFacilityIdAndId(facilityId,vaccinationId)){
                        return  false;
                    }
                }
            }
        }
        if(scanner.hasNext() && vaccinationId == -1 ) {
            item = scanner.next();
            if (item.equals("vaccinations") ) {
                if (scanner.hasNextInt()) {
                    vaccinationId = scanner.nextInt();
                    if (!vaccinationEventRepository.existsByPatientIdAndId(patientId,vaccinationId)){
                        return  false;
                    }
                }
            }
        }
        return true;

    }
}