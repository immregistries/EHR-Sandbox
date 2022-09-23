package org.immregistries.ehr.fhir;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.http.client.HttpClient;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.EhrApiApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;


public class SubscriptionProvider implements IResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionProvider.class);
    /**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
    @Override
    public Class<Subscription> getResourceType() {
        return Subscription.class;
    }

    @Create()
    public MethodOutcome createSubscription(@ResourceParam Subscription subscription) {
        logger.info("Received request CREATE SUBSCRIPTION {}", subscription.getId());
        return new MethodOutcome();
    }

    @Read()
    public Subscription readSubscription(HttpServletRequest request, @IdParam IdType id) {
        OperationOutcome operationOutcome = new OperationOutcome();
        Subscription sub = new Subscription();
        return sub;
    }



}
