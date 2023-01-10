package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Patient;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.PatientRepository;
import org.immregistries.ehr.logic.mapping.PatientMapperR4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Optional;

@Controller
/**
 * NOT FINISHED
 */
public class RelatedPersonProvider implements IResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(RelatedPersonProvider.class);

    @Autowired
    private PatientMapperR4 patientHandler;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Override
    public Class<RelatedPerson> getResourceType() {
        return RelatedPerson.class;
    }

    @Create
    public MethodOutcome createPatient(@ResourceParam RelatedPerson relatedPerson, RequestDetails requestDetails) {
        return createRelatedPerson(relatedPerson,
                facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    public MethodOutcome createRelatedPerson(RelatedPerson relatedPerson, Facility facility) {
        MethodOutcome methodOutcome = new MethodOutcome();
        Optional<Patient> dbPatient = patientRepository.findByFacilityIdAndId(
                facility.getId(),
                Integer.parseInt(relatedPerson.getPatient().getReference()));
        if (dbPatient.isPresent()) {
            HumanName gardianName = relatedPerson.getNameFirstRep();
            if (gardianName.getGiven().size() > 0) {
                dbPatient.get().setGuardianFirst(gardianName.getGiven().get(0).getValue());
            } else {
                dbPatient.get().setGuardianFirst("");
            }
            if (gardianName.getGiven().size() > 1) {
                dbPatient.get().setGuardianMiddle(gardianName.getGiven().get(1).getValue());
            } else {
                dbPatient.get().setGuardianMiddle("");
            }
            dbPatient.get().setGuardianLast(gardianName.getFamily());
        }

        // TODO set received information status and make sure history of patient info if already exists
        Patient saved = patientRepository.save(dbPatient.get());
        return methodOutcome.setId(new IdType().setValue(saved.getId().toString()));
    }

}
