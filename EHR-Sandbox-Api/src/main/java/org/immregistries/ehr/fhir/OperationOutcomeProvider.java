package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.entities.Feedback;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.FeedbackRepository;
import org.immregistries.ehr.repositories.PatientRepository;
import org.immregistries.ehr.repositories.VaccinationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Service
@Controller
public class OperationOutcomeProvider implements IResourceProvider {
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;

    private static final Logger logger = LoggerFactory.getLogger(OperationOutcomeProvider.class);


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

    @Create
    // Endpoint for Subscription
    public MethodOutcome registerOperationOutcome(
            RequestDetails theRequestDetails,
            @ResourceParam OperationOutcome operationOutcome) {
//        String[] ids = CustomIdentificationStrategy.deconcatenateIds(theRequestDetails.getTenantId());
//        Integer tenantId = Integer.parseInt(ids[0]);
//        Integer facilityId = Integer.parseInt(ids[1]);
        // TODO Security checks, secrets ib headers or bundle (maybe in interceptors)
        List<Feedback> feedbackList = new ArrayList<Feedback>();
        String next;
        for (OperationOutcome.OperationOutcomeIssueComponent issue: operationOutcome.getIssue()) {
            Feedback feedback = new Feedback();
            feedback.setContent(issue.toString());
            // Using deprecated field "Location to refer to the right resource for the issue"
            for (StringType location: issue.getLocation()) {
                Scanner scanner = new Scanner(location.getValueNotNull());
                scanner.useDelimiter("/|\\?|#");
                while (scanner.hasNext()) {
                    next = scanner.next();
                    if (next.equals("Patient") && scanner.hasNextInt()) {
                        Optional<Patient> patient = patientRepository.findById(scanner.nextInt());
                        if(patient.isPresent()){
                            feedback.setPatient(patient.get());
                            feedback.setFacility(patient.get().getFacility());
                        }
                    }
                    if (next.equals("Immunization") && scanner.hasNextInt()) {
                        Optional<VaccinationEvent> vaccinationEvent = vaccinationEventRepository.findById(scanner.nextInt());
                        vaccinationEvent.ifPresent(feedback::setVaccinationEvent);
                        if(vaccinationEvent.isPresent()){
                            feedback.setVaccinationEvent(vaccinationEvent.get());
                            feedback.setPatient(vaccinationEvent.get().getPatient());
                            feedback.setFacility(vaccinationEvent.get().getAdministeringFacility());
                        }
                    }
                }
            }
            feedbackList.add(feedback);
        }
        feedbackRepository.saveAll(feedbackList);
        return new MethodOutcome().setOperationOutcome(operationOutcome);
//      return operationOutcome;
    }
}
