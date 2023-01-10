package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.StringType;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;

@Controller
public class OperationOutcomeProviderR5 implements IResourceProvider {
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private SubscriptionStoreRepository subscriptionStoreRepository;
    private static final Logger logger = LoggerFactory.getLogger(OperationOutcomeProviderR5.class);

    @Override
    public Class<OperationOutcome> getResourceType() {
        return OperationOutcome.class;
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
        Integer facilityId = Integer.parseInt(theRequestDetails.getTenantId());
        operationOutcome.setId(id);
        operationOutcome.addIssue().setCode(OperationOutcome.IssueType.VALUE);
        return operationOutcome;
    }

    @Update
    public MethodOutcome updateOperationOutcome(
            @ResourceParam OperationOutcome operationOutcome,
            RequestDetails theRequestDetails,
            HttpServletRequest request
    ) {
        return registerOperationOutcome(operationOutcome,theRequestDetails,request);
    }


    @Create
    // Endpoint for Subscription
    public MethodOutcome registerOperationOutcome(
            @ResourceParam OperationOutcome operationOutcome,
            RequestDetails theRequestDetails,
            HttpServletRequest request
            ) {
//        String[] ids = CustomIdentificationStrategy.deconcatenateIds(theRequestDetails.getTenantId());
//        Integer tenantId = Integer.parseInt(ids[0]);
        Facility facility = facilityRepository.findById(Integer.parseInt(theRequestDetails.getTenantId())).get();
        List<Feedback> feedbackList = new ArrayList<Feedback>();
        String next;

        for (OperationOutcome.OperationOutcomeIssueComponent issue: operationOutcome.getIssue()) {
            Feedback feedback = new Feedback();
            feedback.setContent(issue.getDetails().getText());
            feedback.setFacility(facility);
            feedback.setSeverity(issue.getSeverity().toCode());
            feedback.setCode(issue.getCode().toCode());
            feedback.setTimestamp(new Timestamp(new Date().getTime()));
            if (request != null && request.getRemoteAddr() != null) {
                Optional<ImmunizationRegistry> immunizationRegistry = immunizationRegistryRepository.findByUserIdAndIisFhirUrl(Integer.parseInt(theRequestDetails.getTenantId()),request.getRemoteAddr());
                if (immunizationRegistry.isPresent()) {
                    feedback.setIis(immunizationRegistry.get().getName());
                } else {
                    feedback.setIis(request.getRemoteAddr());
                }
            }
            // Using deprecated field "Location to refer to the right resource for the issue"
            for (StringType location: issue.getLocation()) {
                Scanner scanner = new Scanner(location.getValueNotNull());
                scanner.useDelimiter("/|\\?|#");
                while (scanner.hasNext()) {
                    next = scanner.next();
                    if (next.equals("Patient") && scanner.hasNextInt()) { // TODO Define format for identifier specification, or use fhirpath more accurately
                        Optional<Patient> patient = patientRepository.findByFacilityIdAndId(facility.getId(),scanner.nextInt());
                        patient.ifPresent(feedback::setPatient);
                    }
                    if (next.equals("Immunization") && scanner.hasNextInt()) {
                        Optional<VaccinationEvent> vaccinationEvent = vaccinationEventRepository.findByAdministeringFacilityIdAndId(facility.getId(), scanner.nextInt());
                        if(vaccinationEvent.isPresent()){
                            feedback.setVaccinationEvent(vaccinationEvent.get());
                            feedback.setPatient(vaccinationEvent.get().getPatient());
                        }
                    }
                }
            }
            feedbackList.add(feedback);
        }
        feedbackRepository.saveAll(feedbackList);
        return new MethodOutcome().setOperationOutcome(operationOutcome).setCreated(true).setResource(operationOutcome);
    }
}
