package org.immregistries.ehr.api;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.RevisionListener;
import org.immregistries.ehr.api.entities.AuditRevisionEntity;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.UserRepository;
import org.immregistries.ehr.api.security.UserDetailsImpl;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuditRevisionListener implements RevisionListener, ApplicationContextAware {
    public static final String IMMUNIZATION_REGISTRY_ID = "immunization_registry_id";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final String USER_ID = "user_id";
    public static final String TENANT_NAME = "TENANT_NAME";
    public static final String COPIED_ENTITY_ID = "copied_entity_id";
    public static final String COPIED_FACILITY_ID = "copied_facility_id";
    Logger logger = LoggerFactory.getLogger(AuditRevisionListener.class);

    private UserDetailsServiceImpl userDetailsService;
    private FacilityRepository facilityRepository;
    private UserRepository userRepository;
    private ApplicationContext applicationContext;

    private void init() {
        this.userDetailsService = applicationContext.getBean(UserDetailsServiceImpl.class);
        this.userRepository = applicationContext.getBean(UserRepository.class);
        this.facilityRepository = applicationContext.getBean(FacilityRepository.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void newRevision(Object revisionEntity) {
        if (facilityRepository == null) {
            init();
        }
        AuditRevisionEntity audit = (AuditRevisionEntity) revisionEntity;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//        for (Iterator<String> it = request.getAttributeNames().asIterator(); it.hasNext(); ) {
//            String name = it.next();
//            logger.info("Request {} = {}", name, request.getAttribute(name));
//        }

        try {
            /**
             * Reading values set by FHIR server interceptors and providers
             */
            if (request.getAttribute(IMMUNIZATION_REGISTRY_ID) != null) {
                audit.setImmunizationRegistryId((int) request.getAttribute(IMMUNIZATION_REGISTRY_ID));
            }
            if (request.getAttribute(SUBSCRIPTION_ID) != null) {
                audit.setSubscriptionId((String) request.getAttribute(SUBSCRIPTION_ID));
            }
            if (request.getAttribute(USER_ID) != null) {
                audit.setUser((Integer) request.getAttribute(USER_ID));
            }

        } catch (ClassCastException e) {
        }

        /**
         * To retrace origin when local copy functionality is used
         */
        try {
            if (StringUtils.isNotBlank(request.getParameter(COPIED_ENTITY_ID))) {
                audit.setCopiedEntityId(request.getParameter(COPIED_ENTITY_ID));
            }
            if (StringUtils.isNotBlank(request.getParameter(COPIED_FACILITY_ID))) {
                String copiedFacilityId = request.getParameter(COPIED_FACILITY_ID);
                UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (facilityRepository.existsByUserIdAndId(userDetails.getId(), copiedFacilityId)) {
                    audit.setCopiedFacilityId(copiedFacilityId);
                }
            }
        } catch (ClassCastException e) {
        }

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            audit.setUser(userDetails.getId());
        } catch (ClassCastException e) {
        }
    }
}