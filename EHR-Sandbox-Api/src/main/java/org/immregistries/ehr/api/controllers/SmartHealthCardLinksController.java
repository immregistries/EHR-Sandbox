package org.immregistries.ehr.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.dtos.ReceivedHistoryDTO;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.shlink.service.SmartHealthCardParser;
import org.immregistries.ehr.shlink.service.SmartHealthLinksService;
import org.immregistries.ehr.shlink.service.VerifiableCredentialBundleExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.PublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;
import static org.immregistries.ehr.shlink.SmartHealthConstants.CREDENTIAL_SUBJECT;
import static org.immregistries.ehr.shlink.SmartHealthConstants.SHLINK_PREFIX;
import static org.immregistries.ehr.shlink.model.ShCardClaims.VC;

@Controller
public class SmartHealthCardLinksController {
    public static final String PASSWORD = "password";
    public static final String JWK = "jwk";
    Logger logger = LoggerFactory.getLogger(SmartHealthCardLinksController.class);

    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private SmartHealthLinksService smartHealthLinksService;
    @Autowired
    private SmartHealthCardParser smartHealthCardParser;
    @Autowired
    private VerifiableCredentialBundleExtractor verifiableCredentialBundleExtractor;

    @PostMapping(TENANT_ID_PATH + "/$read-sh-link")
    public ResponseEntity<List<String>> displayHealthLink(@RequestBody() String url, @RequestParam(PASSWORD) Optional<String> password, @RequestParam(JWK) Optional<String> jwk) throws JsonProcessingException {
        List<String> body = readHealthLink(url, password, jwk);
        return ResponseEntity.ok(body);
    }

    @PostMapping(PATIENT_ID_PATH + "/$import-sh-link")
    public ResponseEntity<ReceivedHistoryDTO> importSmartHealthLink(
            @PathVariable(FACILITY_ID) Integer facilityId,
            @PathVariable(PATIENT_ID) Integer patientId,
            @RequestBody() String url, @RequestParam(PASSWORD) Optional<String> password, @RequestParam(JWK) Optional<String> jwk) throws JsonProcessingException {
        List<String> body = readHealthLink(url, password, jwk);
        List<VaccinationEvent> vaccinationEvents = new ArrayList<>(10);
        ReceivedHistoryDTO receivedHistoryDTO = new ReceivedHistoryDTO();

        for (String str : body) {
            JsonElement jsonElement = JsonParser.parseString(str);
            JsonObject vc = null;
            if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(VC)) {
                vc = jsonElement.getAsJsonObject().getAsJsonObject(VC);
            } else if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(CREDENTIAL_SUBJECT)) {
                vc = jsonElement.getAsJsonObject();
            }
            if (vc != null) {
                receivedHistoryDTO.setPatient(verifiableCredentialBundleExtractor.parseBundlePatientFromVC(vc));
                vaccinationEvents.addAll(verifiableCredentialBundleExtractor.parseBundleVaccinationsFromVC(vc));
            } else {
                vaccinationEvents.addAll(verifiableCredentialBundleExtractor.parseBundleVaccinationsUnknownVersion(str));
                // TODO figure bundle version and parse
            }
        }
        receivedHistoryDTO.setVaccinationEvents(vaccinationEvents);

        return ResponseEntity.ok(receivedHistoryDTO);
    }

    private List<String> readHealthLink(String url, Optional<String> password, Optional<String> jwkString) throws JsonProcessingException {
        if (!url.contains(SHLINK_PREFIX)) {
            throw new RuntimeException("Invalid shlink");
        }
        String shlink = SHLINK_PREFIX + url.trim().split(SHLINK_PREFIX)[1];
        PublicKey publicKey = null;
        if (StringUtils.isNotBlank(jwkString.orElse(null))) {
            JWK jwk = null;
            try {
                jwk = com.nimbusds.jose.jwk.JWK.parse(jwkString.get());
                KeyType keyType = jwk.getKeyType();
                if (keyType.equals(KeyType.EC)) {
                    publicKey = jwk.toECKey().toPublicKey();
                } else if (keyType.equals(KeyType.RSA)) {
                    publicKey = jwk.toRSAKey().toPublicKey();
                } else if (keyType.equals(KeyType.OCT) || keyType.equals(KeyType.OKP)) {
                    publicKey = jwk.toOctetKeyPair().toPublicKey();
                }
            } catch (JOSEException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
        List<String> body = smartHealthLinksService.importSmartHealthLink(shlink, password.orElse(null), publicKey, "EHR-sandbox-test");
        return body;
    }


}
