package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.ResourceType;
import org.hl7.fhir.r5.model.StringType;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Feedback;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.fhir.EhrFhirProvider;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller

public class OperationOutcomeProviderR5 implements IResourceProvider, EhrFhirProvider<OperationOutcome> {
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private EhrSubscriptionRepository ehrSubscriptionRepository;

    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    private static final Logger logger = LoggerFactory.getLogger(OperationOutcomeProviderR5.class);

    @Override
    public Class<OperationOutcome> getResourceType() {
        return OperationOutcome.class;
    }

    public ResourceType getResourceName() {
        return ResourceType.OperationOutcome;
    }

    @Search
    public OperationOutcome search(RequestDetails theRequestDetails) {
        logger.info(theRequestDetails.getFhirServerBase());
        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue().setCode(OperationOutcome.IssueType.VALUE);
        return operationOutcome;
    }

    @Read
    public OperationOutcome read(
            RequestDetails theRequestDetails,
            @IdParam IdType id) {
        OperationOutcome operationOutcome = new OperationOutcome();
        String facilityId = theRequestDetails.getTenantId();
        operationOutcome.setId(id);
        operationOutcome.addIssue().setCode(OperationOutcome.IssueType.VALUE);
        return operationOutcome;
    }

    @Update
    public MethodOutcome update(
            @ResourceParam OperationOutcome operationOutcome,
            ServletRequestDetails theRequestDetails
    ) {
        return create(operationOutcome, theRequestDetails);
    }


    @Create
    // Endpoint for Subscription
    public MethodOutcome create(
            @ResourceParam OperationOutcome operationOutcome,
            ServletRequestDetails theRequestDetails
    ) {
        HttpServletRequest request = theRequestDetails.getServletRequest();
        Optional<ImmunizationRegistry> immunizationRegistry = Optional.empty();
        if (request != null && request.getRemoteAddr() != null) {
            immunizationRegistry = immunizationRegistryRepository.findByUserIdAndIisFhirUrl(Integer.parseInt(theRequestDetails.getTenantId()), request.getRemoteAddr()); //TODO change this and do smtg similar to immunizationprovider
        }
        return update(operationOutcome, theRequestDetails, immunizationRegistry.orElse(null));
    }


    public MethodOutcome update(
            @ResourceParam OperationOutcome operationOutcome,
            ServletRequestDetails requestDetails,
            ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(requestDetails.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        List<Feedback> feedbackList = new ArrayList<Feedback>();
        String next;

        for (OperationOutcome.OperationOutcomeIssueComponent issue : operationOutcome.getIssue()) {
            Feedback feedback = new Feedback();
            feedback.setContent(issue.getDetails().getText());
            feedback.setFacility(facility);
            feedback.setSeverity(issue.getSeverity().toCode());
            feedback.setCode(issue.getCode().toCode());
            feedback.setTimestamp(new Timestamp(new Date().getTime()));
            if (immunizationRegistry != null) {
                feedback.setIis(immunizationRegistry.getName());
            } else {
                feedback.setIis(requestDetails.getServletRequest().getRemoteAddr());
            }
            /**
             * Using deprecated field "Location to refer to the right resource for the issue"
             */
            for (StringType location : issue.getLocation()) {
                String localUrl = resourceIdentificationService.getLocalUrnFromUrn(location.getValueNotNull(), immunizationRegistry, facility);
                if (localUrl != null) {
                    String[] idArray = localUrl.split("/");
                    if (idArray[0].equals("Patient")) {
                        patientRepository.findByFacilityIdAndId(facility.getId(), idArray[1])
                                .ifPresent(feedback::setPatient);
                    } else if (idArray[0].equals("Immunization")) {
                        Optional<VaccinationEvent> vaccinationEvent = vaccinationEventRepository.findByAdministeringFacilityIdAndId(facility.getId(), idArray[1]);
                        if (vaccinationEvent.isPresent()) {
                            feedback.setVaccinationEvent(vaccinationEvent.get());
                            feedback.setPatient(vaccinationEvent.get().getPatient());
                        }
                    }
                }
            }
            feedbackList.add(feedback);
        }
        feedbackRepository.saveAll(feedbackList);
        return new MethodOutcome().setCreated(true).setResource(operationOutcome);
    }
}
