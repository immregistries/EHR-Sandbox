package org.immregistries.ehr.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.JwkCacheService;
import org.immregistries.ehr.api.dtos.ReceivedHistoryDTO;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.security.UserDetailsImpl;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.shlink.service.SmartHealthCardParser;
import org.immregistries.ehr.shlink.service.SmartHealthLinksService;
import org.immregistries.ehr.shlink.service.VerifiableCredentialBundleExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.PATIENT_ID_PATH;
import static org.immregistries.ehr.api.controllers.ControllerHelper.TENANT_ID_PATH;
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

    @Autowired
    JwkCacheService jwkCacheService;

    @PostMapping(TENANT_ID_PATH + "/$read-sh-link")
    public ResponseEntity<List<String>> displayHealthLink(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @RequestBody() String url, @RequestParam(PASSWORD) Optional<String> password, @RequestParam(JWK) Optional<String> jwk) throws JsonProcessingException {
        List<String> body = readHealthLink(url, password, jwk, userPrincipal.getId());
        return ResponseEntity.ok(body);
    }

    @PostMapping({TENANT_ID_PATH + "/$import-sh-link", PATIENT_ID_PATH + "/$import-sh-link"})
    public ResponseEntity<ReceivedHistoryDTO> importSmartHealthLink(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
//            @PathVariable(FACILITY_ID) Integer facilityId,
//            @PathVariable(PATIENT_ID) Integer patientId,
            @RequestBody() String url, @RequestParam(PASSWORD) Optional<String> password, @RequestParam(JWK) Optional<String> jwk) throws JsonProcessingException {
        List<String> body = readHealthLink(url, password, jwk, userPrincipal.getId());
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

    @PostMapping(TENANT_ID_PATH + "/$sh-link-jwk")
    public ResponseEntity<JWKSet> storeJWK(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @RequestBody String jwk) {
        return ResponseEntity.ok(parseAndSaveJWK(jwk, userPrincipal.getId()));
    }

    private List<String> readHealthLink(String url, Optional<String> password, Optional<String> jwkString, Integer userId) throws JsonProcessingException {
        if (!url.contains(SHLINK_PREFIX)) {
            throw new RuntimeException("Invalid shlink");
        }
        String shLink = SHLINK_PREFIX + url.trim().split(SHLINK_PREFIX)[1];
        JWKSet jwkSet = parseAndSaveJWK(jwkString.orElse(null), userId);
        List<String> body = smartHealthLinksService.importSmartHealthLink(shLink, password.orElse(null), jwkSet, "EHR-sandbox-test");
        return body;
    }

    private JWKSet parseAndSaveJWK(String jwkString, Integer userId) {
        if (StringUtils.isNotBlank(jwkString)) {
            try {
                JWK jwk = com.nimbusds.jose.jwk.JWK.parse(jwkString);
                jwkCacheService.addSingleKeyToCache(userId.toString(), jwk);
                return jwkCacheService.getJwkSetForUser(userId.toString(), "");
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }


}
