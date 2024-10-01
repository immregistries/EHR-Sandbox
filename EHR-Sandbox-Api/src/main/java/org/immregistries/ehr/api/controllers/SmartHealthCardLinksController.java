package org.immregistries.ehr.api.controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.fhir.Client.SmartHealthCardService;
import org.immregistries.ehr.fhir.Client.SmartHealthLinksService;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
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

import static org.immregistries.ehr.api.controllers.ControllerHelper.PATIENT_ID_PATH;
import static org.immregistries.ehr.api.controllers.ControllerHelper.TENANT_ID_PATH;
import static org.immregistries.ehr.fhir.Client.SmartHealthCardService.CREDENTIAL_SUBJECT;
import static org.immregistries.ehr.fhir.Client.SmartHealthCardService.VC;
import static org.immregistries.ehr.fhir.Client.SmartHealthLinksService.SHLINK_PREFIX;

@Controller
public class SmartHealthCardLinksController {
    Logger logger = LoggerFactory.getLogger(SmartHealthCardLinksController.class);

    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private SmartHealthLinksService smartHealthLinksService;
    @Autowired
    private SmartHealthCardService smartHealthCardService;

    @PostMapping(TENANT_ID_PATH + "/$read-shlink")
    public ResponseEntity<List<String>> displayHealthLink(@RequestBody() String url, @RequestParam Optional<String> password, @RequestParam Optional<String> jwk) {
        List<String> body = readHealthLink(url, password, jwk);
        return ResponseEntity.ok(body);
    }

    @PostMapping(PATIENT_ID_PATH + "/$import-shlink")
    public ResponseEntity<List<VaccinationEvent>> importSmartHealthLink(
            @PathVariable Integer facilityId,
            @PathVariable() Integer patientId,
            @RequestBody() String url, @RequestParam Optional<String> password, @RequestParam Optional<String> jwk) {
        List<String> body = readHealthLink(url, password, jwk);
        List<VaccinationEvent> vaccinationEvents = new ArrayList<>(10);
        for (String str : body) {
            JsonElement jsonElement = JsonParser.parseString(str);
            logger.info("parse result {}", str);
            JsonObject vc = null;
            if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(VC)) {
                vc = jsonElement.getAsJsonObject().getAsJsonObject("VC");
            } else if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(CREDENTIAL_SUBJECT)) {
                vc = jsonElement.getAsJsonObject();
            }
            if (vc != null) {
                vaccinationEvents.addAll(smartHealthCardService.parseBundleVaccinationsFromVC(vc));
            } else {
                vaccinationEvents.addAll(smartHealthCardService.parseBundleVaccinationsUnknownVersion(str));
                // TODO figure bundle version and parse
            }
        }
        return ResponseEntity.ok(vaccinationEvents);
    }

    private List<String> readHealthLink(String url, Optional<String> password, Optional<String> jwkString) {
        if (!url.contains(SHLINK_PREFIX)) {
            throw new RuntimeException("Invalid shlink");
        }
        String shlink = SHLINK_PREFIX + url.split(SHLINK_PREFIX)[1];
        PublicKey publicKey = null;
        if (StringUtils.isNotBlank(jwkString.orElse(null))) {
            JWK jwk = null;
            try {
                jwk = JWK.parse(jwkString.get());
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
        List<String> body = smartHealthLinksService.importSmartHealthLink(shlink, password.orElse(null), publicKey);
        return body;
    }


}
