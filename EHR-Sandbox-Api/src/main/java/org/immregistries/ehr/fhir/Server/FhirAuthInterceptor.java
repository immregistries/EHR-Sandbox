package org.immregistries.ehr.fhir.Server;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.hl7.fhir.r5.model.Bundle;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.repositories.UserRepository;
import org.immregistries.ehr.api.security.JwtUtils;
import org.immregistries.ehr.api.security.UserDetailsImpl;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.immregistries.ehr.api.AuditRevisionListener.TENANT_NAME;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;

/**
 * Incomplete, currently used for tracking of modifying users in envers framework for history
 */
@Interceptor
@Component
public class FhirAuthInterceptor extends AuthorizationInterceptor {

    Logger logger = LoggerFactory.getLogger(FhirAuthInterceptor.class);
    @Autowired
    DaoAuthenticationProvider authenticationManager;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authHeader = theRequestDetails.getHeader("Authorization");

        try {
            String jwt = null;
            if (StringUtils.hasText(authHeader)) {
                String username = null;
                UserDetailsImpl userDetails = null;
                if (authHeader.startsWith("Bearer ")) {
                    jwt = authHeader.substring(7);
                    if (jwtUtils.validateJwtToken(jwt)) {
                        username = jwtUtils.getUserNameFromJwtToken(jwt);
                        userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
                /**
                 * Basic authentication
                 */
                if (authHeader.startsWith("Basic ")) {
                    String base64 = authHeader.substring("Basic ".length());
                    String base64decoded = new String(Base64.decodeBase64(base64));
                    String[] parts = base64decoded.split(":");
                    username = parts[0];
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(username, parts[1]));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    userDetails = (UserDetailsImpl) authentication.getPrincipal();
                }

                if (userDetails != null && username != null) {
                    theRequestDetails.setAttribute(USER_ID, userDetails.getId());
                    facilityRepository.findById(theRequestDetails.getTenantId()).orElseThrow(
                            () -> new InvalidRequestException("TENANT ID not recognised")
                    );
                    request.setAttribute(TENANT_NAME, facilityRepository.findById(theRequestDetails.getTenantId()).orElseThrow(
                            () -> new InvalidRequestException("TENANT ID not recognised")
                    ).getTenant().getId());
                    // TODO IMMUNIZATION REGISTRY IDENTIFICATION and set attribute IMMUNIZATION_REGISTRY_ID

                    /**
                     * Each 'FHIR Tenant' matches a facility, allowing operations for facility owned by user
                     */
                    String[] facilityIds = StreamSupport
                            .stream(facilityRepository
                                    .findByUser(userRepository.findById(userDetails.getId()).get()).spliterator(), false)
                            .map(Facility::getId).map(Object::toString).toArray(String[]::new);
//                    for (String id: facilityIds
//                    ) {
//                        logger.info(" allowed {}", id);
//                    }
                    return new RuleBuilder()
                            .allowAll("Logged in as " + username)
                            .forTenantIds(facilityIds)
                            .build();
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        /**
         * Authentication for bundle is dealt with in provider because of subscription specific authentication
         * attributes are currently set in BundleProvider,
         *         theRequestDetails.getServletRequest().setAttribute(AuditRevisionListener.IMMUNIZATION_REGISTRY_ID,-1);
         *         theRequestDetails.getServletRequest().setAttribute(AuditRevisionListener.SUBSCRIPTION_ID,"-1");
         *         theRequestDetails.getServletRequest().setAttribute(AuditRevisionListener.USER_ID,"-1");
         */
        return new RuleBuilder().allow().create().resourcesOfType(Bundle.class)
                .withAnyId()
//                .withCodeInValueSet("type", Bundle.BundleType.SUBSCRIPTIONNOTIFICATION.toString())
//                .withFilter("Bundle?type=" + Bundle.BundleType.SUBSCRIPTIONNOTIFICATION)
                .andThen()
                .denyAll("Missing or invalid Authorization header value")
                .build();
    }

}
