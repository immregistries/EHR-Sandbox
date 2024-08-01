package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/registry")
public class ImmunizationRegistryController {

    private static final Logger logger = LoggerFactory.getLogger(ImmunizationRegistryController.class);

    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private CustomClientFactory customClientFactory;

    @GetMapping({"/{id}"})
    public ImmunizationRegistry getImmunizationRegistry(@PathVariable() Integer id) {
        Optional<ImmunizationRegistry> immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(id, userDetailsService.currentUserId());
        if (immunizationRegistry.isPresent()) {
            return immunizationRegistry.get();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    @GetMapping({"/{id}/metadata"})
    public ResponseEntity<String> getImmunizationRegistryMetadata(@PathVariable() Integer id) {
        IGenericClient client = customClientFactory.newGenericClient(id);
        CapabilityStatement capabilityStatement;
        try {
            capabilityStatement = client.capabilities().ofType(CapabilityStatement.class).prettyPrint().execute();
        } catch (ResourceVersionConflictException resourceVersionConflictException) {
            /**
             * Conflict might rise because iis is creating tenant, so we give it another try
             */
            capabilityStatement = client.capabilities().ofType(CapabilityStatement.class).prettyPrint().execute();
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
        return ResponseEntity.ok(client.getFhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(capabilityStatement));
    }

    @GetMapping()
    public Iterable<ImmunizationRegistry> getImmRegistries() {
        return immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
    }

    @PutMapping()
    public ImmunizationRegistry putImmunizationRegistry(@RequestBody ImmunizationRegistry immunizationRegistry) {
        Optional<ImmunizationRegistry> old = immunizationRegistryRepository.findByIdAndUserId(immunizationRegistry.getId(), userDetailsService.currentUserId());
        if (old.isPresent()) {
            immunizationRegistry.setUser(userDetailsService.currentUser());
            return immunizationRegistryRepository.save(immunizationRegistry);
        } else {
            return postImmunizationRegistry(immunizationRegistry);
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    @PostMapping()
    public ImmunizationRegistry postImmunizationRegistry(@RequestBody ImmunizationRegistry immunizationRegistry) {
        immunizationRegistry.setUser(userDetailsService.currentUser());
        immunizationRegistry.setId(null);
        if (immunizationRegistryRepository.existsByNameAndUserId(immunizationRegistry.getName(), userDetailsService.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used");
        }
        return immunizationRegistryRepository.save(immunizationRegistry);
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity removeImmunizationRegistry(@PathVariable() Integer id) {
        immunizationRegistryRepository.deleteByIdAndUserId(id, userDetailsService.currentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{registryId}/$connectivity")
    public ResponseEntity<String> checkHl7Connectivity(@PathVariable() Integer registryId) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = this.getImmunizationRegistry(registryId);
        try {
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            if (StringUtils.isNotBlank(immunizationRegistry.getIisUsername())) {
                connector.setUserid(immunizationRegistry.getIisUsername());
                connector.setPassword(immunizationRegistry.getIisPassword());
                connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            }
            String result = connector.connectivityTest("");
            logger.info("Check Connectivity {}", result);
            return ResponseEntity.ok().build();
        } catch (Exception e1) {
            e1.printStackTrace();
            return ResponseEntity.internalServerError().body("SOAP Error: " + e1.getMessage());
        }
    }

    @GetMapping("/{registryId}/$auth")
    public ResponseEntity<String> checkHl7Auth(@PathVariable() Integer registryId) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = this.getImmunizationRegistry(registryId);
        try {
            String message = "MSH|^~\\&|EHR Sandbox||||||QBP^Q11^QBP_Q11\n" +
                    "QPD|Z34^Request Immunization History^CDCPHINVS|||Doe^John^^^^^L|^^^^^^M|19700101|\n" +
                    "RCP|I|20^RD&Records&HL70126|\n";
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            if (StringUtils.isNotBlank(immunizationRegistry.getIisUsername())) {
                connector.setUserid(immunizationRegistry.getIisUsername());
                connector.setPassword(immunizationRegistry.getIisPassword());
                connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            }
            String result = connector.submitMessage(message, false);
            return ResponseEntity.ok().build();
        } catch (Exception e1) {
            e1.printStackTrace();
            return ResponseEntity.internalServerError().body("SOAP Error: " + e1.getMessage());
        }
    }


}
