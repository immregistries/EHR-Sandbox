package org.immregistries.ehr.fhir.server;

import org.immregistries.ehr.api.entities.User;
import org.immregistries.ehr.api.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class ServerHelper {
    /**
     * Used for sql requests in FHIR Server
     *
     * @return copy without password of User
     */
    public static User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
            User user = new User();
            user.setId(userDetailsImpl.getId());
            user.setUsername(user.getUsername());
            return user;
        }
        return null;// TODO throw exception
    }

//    public static Tenant currentTenant(RequestDetails requestDetails) {
//        Tenant tenant
//        requestDetails.getAttribute(TENANT_ID);
//        return null;
//    }
}
