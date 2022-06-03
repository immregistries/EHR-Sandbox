package org.immregistries.ehr.fhir;

import ca.uhn.fhir.i18n.HapiLocalizer;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.tenant.ITenantIdentificationStrategy;
import ca.uhn.fhir.util.UrlPathTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomIdentificationStrategy implements ITenantIdentificationStrategy {
    private static final Logger ourLog = LoggerFactory.getLogger(CustomIdentificationStrategy.class);

    public CustomIdentificationStrategy() {
    }

    public void extractTenant(UrlPathTokenizer theUrlPathTokenizer, RequestDetails theRequestDetails) {
        String token = null;
        String concatenatedIds = null;
        while (theUrlPathTokenizer.hasMoreTokens()) {
            token = (String) StringUtils.defaultIfBlank(theUrlPathTokenizer.nextTokenUnescapedAndSanitized(), (CharSequence)null);
            if (token.equals("tenants")) {
                concatenatedIds = (String) StringUtils.defaultIfBlank(theUrlPathTokenizer.nextTokenUnescapedAndSanitized(), (CharSequence)null);
            }
            if (token.equals("facilities") && theUrlPathTokenizer.hasMoreTokens()) {
                concatenatedIds += "," + (String) StringUtils.defaultIfBlank(theUrlPathTokenizer.nextTokenUnescapedAndSanitized(), (CharSequence)null);
            }
        }
        ourLog.info("Found tenant ID {} in request string", concatenatedIds);

        theRequestDetails.setTenantId(concatenatedIds);


        if (concatenatedIds == null) {
            HapiLocalizer localizer = theRequestDetails.getServer().getFhirContext().getLocalizer();
            throw new InvalidRequestException(Msg.code(307) + localizer.getMessage(RestfulServer.class, "rootRequest.multitenant", new Object[0]));
        }
    }

    public String massageServerBaseUrl(String theFhirServerBase, RequestDetails theRequestDetails) {
        Validate.notNull(theRequestDetails.getTenantId(), "theTenantId is not populated on this request", new Object[0]);
        String[] ids = deconcatenateIds(theRequestDetails.getTenantId());
        return theFhirServerBase + "/tenants/" + ids[0] + "/facilities/" + ids[1];
//        return theFhirServerBase;
    }

    /**
     * Extract ids from endpoint url
     * @param idsString Tenant Id as stored in RequestDetails
     * @return [tenantId, facilityId]
     */
    public static String[] deconcatenateIds(String idsString) {
        return idsString.split(",");
    }
}
