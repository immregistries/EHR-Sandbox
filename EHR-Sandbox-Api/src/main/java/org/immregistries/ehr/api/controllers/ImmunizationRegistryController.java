package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
@RequestMapping(REGISTRY_PATH_HEADER)
public class ImmunizationRegistryController {

    private static final Logger logger = LoggerFactory.getLogger(ImmunizationRegistryController.class);

    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;

    /**
     * Read registry info
     *
     * @param registryId id
     * @return Registry with Id
     */
    @GetMapping({REGISTRY_ID_SUFFIX})
    public ImmunizationRegistry getImmunizationRegistry(@PathVariable(REGISTRY_ID) Integer registryId) {
        return immunizationRegistryService.getImmunizationRegistry(registryId);
    }

    /**
     * Fetch FHIR metadata from Server
     *
     * @param registryId registry id
     * @return fetched FHIR metadata
     */
    @GetMapping({REGISTRY_ID_SUFFIX + "/metadata"})
    public ResponseEntity<String> getImmunizationRegistryMetadata(@PathVariable(REGISTRY_ID) Integer registryId) {
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(getImmunizationRegistry(registryId));
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

    /**
     * Get all ImmunizationRegistries
     *
     * @return All ImmunizationRegistries for User
     */
    @GetMapping()
    public ResponseEntity<?> getAllImmunizationRegistries(@RequestParam(value = "name", required = false) String name) {
//        Map<String, String> envVars = System.getenv();
//
//        logger.info("All Environment Variables: {} {}", System.getenv("PROXY_HOST_SOAP"), System.getenv("PROXY_PORT_SOAP"));
//        logger.info("All Environment Variables:");
//        logger.info("All Environment Variables:");
//        for (Map.Entry<String, String> entry : envVars.entrySet()) {
//            logger.info("{} = {}", entry.getKey(), entry.getValue());
//        }
        if (StringUtils.isNotBlank(name)) {
            return ResponseEntity.ok(immunizationRegistryRepository.findByNameAndUserId(name, userDetailsService.currentUserId()));
        }
        return ResponseEntity.ok(immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId(),
                Sort.by(Sort.Order.desc("isDefault"),
                        Sort.Order.desc("name"))));
    }

    /**
     * Modify ImmunizationRegistry
     *
     * @param immunizationRegistry updated ImmunizationRegistry information
     * @return Updated ImmunizationRegistry
     */
    @PutMapping()
    public ImmunizationRegistry putImmunizationRegistry(@RequestBody ImmunizationRegistry immunizationRegistry) {
        Optional<ImmunizationRegistry> old = immunizationRegistryRepository.findByIdAndUserId(immunizationRegistry.getId(), userDetailsService.currentUserId());
        if (old.isPresent()) {
            immunizationRegistry.setUser(userDetailsService.currentUser());
            checkSelectedDefault(immunizationRegistry);
            return immunizationRegistryRepository.save(immunizationRegistry);
        } else {
            return postImmunizationRegistry(immunizationRegistry);
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_ACCEPTABLE, "Invalid id");
        }
    }

    /**
     * Create ImmunizationRegistry
     *
     * @param immunizationRegistry ImmunizationRegistry information
     * @return Created Registry
     */
    @PostMapping()
    public ImmunizationRegistry postImmunizationRegistry(@RequestBody ImmunizationRegistry immunizationRegistry) {
        immunizationRegistry.setUser(userDetailsService.currentUser());
        immunizationRegistry.setId(null);
        if (immunizationRegistryRepository.existsByNameAndUserId(immunizationRegistry.getName(), userDetailsService.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used");
        }
        checkSelectedDefault(immunizationRegistry);
        return immunizationRegistryRepository.save(immunizationRegistry);
    }

    private void checkSelectedDefault(ImmunizationRegistry immunizationRegistry) {
        if (immunizationRegistry.getDefault()) {
            Optional<ImmunizationRegistry> previousDefault = immunizationRegistryRepository.findByUserIdAndIsDefaultTrue(userDetailsService.currentUserId());
            if (previousDefault.isPresent() && !previousDefault.get().getId().equals(immunizationRegistry.getId())) {
                previousDefault.get().setDefault(false);
                immunizationRegistryRepository.save(previousDefault.get());
            }
        }
    }

    @DeleteMapping({REGISTRY_ID_SUFFIX})
    public ResponseEntity removeImmunizationRegistry(@PathVariable(REGISTRY_ID) Integer registryId) {
        immunizationRegistryRepository.deleteByIdAndUserId(registryId, userDetailsService.currentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping(REGISTRY_ID_SUFFIX + "/$connectivity")
    public ResponseEntity<String> checkHl7Connectivity(@PathVariable(REGISTRY_ID) Integer registryId) {
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

    @GetMapping(REGISTRY_ID_SUFFIX + "/$auth")
    public ResponseEntity<String> checkHl7Auth(@PathVariable(REGISTRY_ID) Integer registryId) {
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
