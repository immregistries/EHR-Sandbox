package org.immregistries.ehr.api.repositories;

import org.immregistries.ehr.api.entities.AcknowledgmentObject;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Generated With Gemini
 */
@Repository
public interface AcknowledgmentObjectRepository extends JpaRepository<AcknowledgmentObject, Integer> {

    List<AcknowledgmentObject> findByFacilityId(Integer facilityId);

    List<AcknowledgmentObject> findByPatientId(Integer patientId);

    List<AcknowledgmentObject> findByVaccinationId(Integer vaccinationId);

    //If you need to find by facility object instead of ID
    List<AcknowledgmentObject> findByFacility(Facility facility);

    //If you need to find by patient object instead of ID
    List<AcknowledgmentObject> findByPatient(EhrPatient patient);

    //If you need to find by vaccination object instead of ID
    List<AcknowledgmentObject> findByVaccination(VaccinationEvent vaccination);

}