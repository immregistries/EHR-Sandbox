package org.immregistries.ehr.security;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.PatientRepository;
import org.immregistries.ehr.repositories.TenantRepository;
import org.immregistries.ehr.repositories.VaccinationEventRepository;
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
                authorized = isAuthorizedURI(request.getContextPath());

            } else { // for registration no authorization needed
                authorized = true;
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        if (authorized) {
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
        String[] pathSplit = url.split("/tenants/",2);
        String[] path = url.split("/",2);
        int tenantId = -1;
        int facilityId = -1;
        int patientId = -1;
        int vaccinationId = -1;
        // Parsing the URI
        int i;
//        return true;
        for (i = 0; i < path.length; i++) {
            if ( path[i].equals("tenants")){
                try {
                    tenantId = Integer.parseInt(path[i+1]);
                    if (!tenantRepository.existsByIdAndUserId(tenantId, userDetailsService.currentUserId())){
                        return  false;
                    }
                    break;
                } catch (NumberFormatException e) {
                    break;
                } catch (NullPointerException e){ // If uri stops at /tenants
                    return true;
                }
            }
        }
        int j;
        for (j=i; j < path.length; j++) {
            if ( path[i].equals("facilities")){
                try {
                    facilityId = Integer.parseInt(path[j+1]);
                    if (!facilityRepository.existsByTenantIdAndId(tenantId,facilityId)){
                        return  false;
                    }
                    break;
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage());
                    break;
                } catch (NullPointerException e){ // If uri stops at /facilities
                    logger.error(e.getMessage());
                    return true;
                }
            }
        }
        int k;
        for (k=j; k < path.length; k++) {
            if ( path[i].equals("patients")){
                try {
                    patientId = Integer.parseInt(path[k+1]);
                    if (!patientRepository.existsByFacilityIdAndId(facilityId,patientId)){
                        return  false;
                    }
                    break;
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage());
                    break;
                } catch (NullPointerException e){ // If uri stops at /patients
                    logger.error(e.getMessage());
                    return true;
                }
            }
        }
        int l;
        for (l=k; l < path.length; l++) {
            if ( path[i].equals("vaccinations")){
                try {
                    vaccinationId = Integer.parseInt(path[l+1]);
                    if (!vaccinationEventRepository.existsByPatientIdAndId(patientId,vaccinationId)){
                        return  false;
                    }
                    break;
                } catch (NumberFormatException e) {
                    break;
                } catch (NullPointerException e){ // If uri stops at /patients
                    return true;
                }
            }
        }
        return true;

    }
}