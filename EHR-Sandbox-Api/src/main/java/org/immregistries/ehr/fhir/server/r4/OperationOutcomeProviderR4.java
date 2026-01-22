package org.immregistries.ehr.fhir.server.r4;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.MappingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class OperationOutcomeProviderR4 implements IResourceProvider, EhrFhirProviderR4<OperationOutcome> {
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

    private static final Logger logger = LoggerFactory.getLogger(OperationOutcomeProviderR4.class);

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
        ImmunizationRegistry immunizationRegistry = null;
        if (request != null && StringUtils.isNotBlank(request.getRemoteAddr())) {
            immunizationRegistry = immunizationRegistryRepository.findByUserIdAndIisFhirUrl(Integer.parseInt(theRequestDetails.getTenantId()), request.getRemoteAddr()).orElse(null); //TODO change this and do smtg similar to immunizationprovider
        }
        return update(operationOutcome, theRequestDetails, immunizationRegistry);
    }


    public MethodOutcome update(
            @ResourceParam OperationOutcome operationOutcome,
            ServletRequestDetails requestDetails,
            ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(EhrUtils.convert(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        List<Feedback> feedbackList = new ArrayList<Feedback>();
        String next;
        AcknowledgmentObject acknowledgmentObject = acknowledgmentObject(operationOutcome, immunizationRegistry, facility, null, null);
        for (OperationOutcome.OperationOutcomeIssueComponent issue : operationOutcome.getIssue()) {
            Feedback feedback = getFeedback(issue, immunizationRegistry, facility, requestDetails);
            /**
             * Using deprecated field "Location to refer to the right resource for the issue"
             */
            for (StringType location : issue.getLocation()) {
                String localUrl = resourceIdentificationService.getLocalUrnFromUrn(location.getValueNotNull(), immunizationRegistry, facility);
                if (localUrl != null) {
                    String[] idArray = localUrl.split("/");
                    if (idArray[0].equals(MappingHelper.PATIENT)) {
                        patientRepository.findByFacilityIdAndId(facility.getId(), EhrUtils.convert(idArray[1]))
                                .ifPresent(feedback::setPatient);
                    } else if (idArray[0].equals(MappingHelper.IMMUNIZATION)) {
                        try {
                            Integer vaccinationId = EhrUtils.convert(idArray[1]);
                            Optional<VaccinationEvent> vaccinationEvent = vaccinationEventRepository.findByAdministeringFacilityIdAndId(facility.getId(), vaccinationId);
                            if (vaccinationEvent.isPresent()) {
                                feedback.setVaccinationEvent(vaccinationEvent.get());
                                feedback.setPatient(vaccinationEvent.get().getPatient());
                            }
                        } catch (NumberFormatException numberFormatException) {
                        }
                    }
                }
            }
            feedbackList.add(feedback);
        }
        feedbackRepository.saveAll(feedbackList);
        return new MethodOutcome().setCreated(true).setResource(operationOutcome);
    }

    private static Feedback getFeedback(OperationOutcome.OperationOutcomeIssueComponent issue, ImmunizationRegistry immunizationRegistry, Facility facility, ServletRequestDetails requestDetails) {
        Feedback feedback = new Feedback();
        feedback.setRaw(issue.toString()); // TODO parser
        feedback.setContent(issue.getDetails().getText());
        feedback.setFacility(facility);
        feedback.setSeverity(issue.getSeverity().toCode());
        feedback.setCode(issue.getCode().toCode());
        feedback.setTimestamp(new Timestamp(new Date().getTime()));
        if (immunizationRegistry != null) {
            feedback.setIis(immunizationRegistry.getName());
        } else if (requestDetails != null) {
            feedback.setIis(requestDetails.getServletRequest().getRemoteAddr());
        }
        return feedback;
    }

    public static AcknowledgmentObject acknowledgmentObject(OperationOutcome operationOutcome, ImmunizationRegistry immunizationRegistry, Facility facility, EhrPatient ehrPatient, VaccinationEvent vaccinationEvent) {
        AcknowledgmentObject acknowledgmentObject = new AcknowledgmentObject();
        acknowledgmentObject.setRawResult(operationOutcome.toString()); // TODO parser
        acknowledgmentObject.setMessageId(operationOutcome.getId());
//        acknowledgmentObject.setSenderSoftware(operationOutcome.get); TODO extract from metadata ?
//        acknowledgmentObject.setSender(hl7Reader.getValue(4));
//        acknowledgmentObject.setDestinationSoftware(hl7Reader.getValue(5));
//        acknowledgmentObject.setDestination(hl7Reader.getValue(6));
        acknowledgmentObject.setFacility(facility);
        acknowledgmentObject.setPatient(ehrPatient);
        acknowledgmentObject.addVaccination(vaccinationEvent);
        Timestamp timestamp = null;
        if (operationOutcome.getMeta().getLastUpdated() != null) {
            timestamp = new Timestamp(operationOutcome.getMeta().getLastUpdated().getTime());
        }
        acknowledgmentObject.setTimestamp(timestamp);

        for (OperationOutcome.OperationOutcomeIssueComponent issue : operationOutcome.getIssue()) {
            Feedback feedback = getFeedback(issue, immunizationRegistry, facility, null);
            feedback.setPatient(ehrPatient);
            feedback.setVaccinationEvent(vaccinationEvent);
            feedback.setTimestamp(timestamp);
            switch (issue.getSeverity()) {
                case FATAL:
                case ERROR: {
                    acknowledgmentObject.setMsa_2("AE");
                    feedback.setSeverity("E");
                    acknowledgmentObject.getSortedResult().getErrors().add(feedback);
                    break;
                }
                case WARNING: {
                    acknowledgmentObject.setMsa_2("AW");
                    feedback.setSeverity("W");
                    acknowledgmentObject.getSortedResult().getWarnings().add(feedback);
                    break;
                }
                case INFORMATION:
                case NULL: {
                    feedback.setSeverity("I");
                    acknowledgmentObject.getSortedResult().getInfos().add(feedback);
                    break;
                }
            }
        }
        return acknowledgmentObject;
    }
}
