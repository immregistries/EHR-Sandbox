package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.StringType;
import org.immregistries.ehr.entities.Feedback;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.FeedbackRepository;
import org.immregistries.ehr.repositories.PatientRepository;
import org.immregistries.ehr.repositories.VaccinationEventRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class OperationOutcomeProvider implements IResourceProvider {
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;



    @Override
    public Class<OperationOutcome> getResourceType() {
        return OperationOutcome.class;
    }

    @Create
    public OperationOutcome registerOperationOutcome(@ResourceParam OperationOutcome operationOutcome) {
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
        return operationOutcome;
    }
}
