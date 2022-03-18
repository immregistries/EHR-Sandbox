package org.immregistries.ehr.controllers;

import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.Tenant;
import org.immregistries.ehr.logic.RandomGenerator;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class MainController {

    @GetMapping("/new_patient")
    public Patient patient() {
        return new Patient();
    }

    @GetMapping("/random_patient")
    public Patient random() {
        return RandomGenerator.randomPatient(null,null);
    }

}
