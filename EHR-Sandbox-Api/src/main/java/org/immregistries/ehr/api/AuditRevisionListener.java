package org.immregistries.ehr.api;

import org.hibernate.envers.RevisionListener;
import org.immregistries.ehr.api.entities.AuditRevisionEntity;
import org.immregistries.ehr.api.entities.User;
import org.immregistries.ehr.api.security.UserDetailsImpl;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

@Service
public class AuditRevisionListener implements RevisionListener {
    public static final String IMMUNIZATION_REGISTRY_ID = "immunization_registry_id";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    Logger logger = LoggerFactory.getLogger(AuditRevisionListener.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Override
    public void newRevision(Object revisionEntity) {
        AuditRevisionEntity audit = (AuditRevisionEntity) revisionEntity;
        HttpServletRequest request =  ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//        for (Iterator<String> it = request.getAttributeNames().asIterator(); it.hasNext(); ) {
//            String name = it.next();
//            logger.info("Request {} = {}", name, request.getAttribute(name));
//        }

        try {
            /**
             * Reading values set by FHIR server interceptors and providers
             */
            audit.setImmunizationRegistryId( (int) request.getAttribute(IMMUNIZATION_REGISTRY_ID));
            audit.setSubscriptionId((String) request.getAttribute(SUBSCRIPTION_ID));
        } catch (ClassCastException e) {}

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            audit.setUser(userDetails.getId());
        } catch (ClassCastException e) {
        }
    }
}