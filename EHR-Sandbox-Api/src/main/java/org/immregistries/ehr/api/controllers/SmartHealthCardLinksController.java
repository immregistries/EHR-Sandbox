package org.immregistries.ehr.api.controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.security.Jwks;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.fhir.Client.SmartHealthCardService;
import org.immregistries.ehr.fhir.Client.SmartHealthLinksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;
import static org.immregistries.ehr.fhir.Client.SmartHealthCardService.VC;
import static org.immregistries.ehr.fhir.Client.SmartHealthLinksService.SHLINK_PREFIX;

@Controller
@RequestMapping(FACILITIES_PATH)
public class SmartHealthCardLinksController {

    @Autowired
    private SmartHealthLinksService smartHealthLinksService;
    @Autowired
    private SmartHealthCardService smartHealthCardService;

    @PostMapping("/$display-shlink")
    public ResponseEntity<List<String>> displayHealthLink(
            @PathVariable() String facilityId,
            @RequestBody() String url, @RequestParam Optional<String> password, @RequestParam Optional<String> jwk) {
        List<String> body = readHealthLink(url, password, jwk);
        return ResponseEntity.ok(body);
    }

    @PostMapping(PATIENT_PATH_HEADER + PATIENT_ID_SUFFIX + "/$import-shlink")
    public ResponseEntity<List<VaccinationEvent>> importSmartHealthLink(
            @PathVariable() String facilityId,
            @RequestBody() String url, @RequestParam Optional<String> password, @RequestParam Optional<String> jwk) {
        List<String> body = readHealthLink(url, password, jwk);
        List<VaccinationEvent> vaccinationEvents = new ArrayList<>(10);
        for (String str : body
        ) {
            JsonElement jsonElement = JsonParser.parseString(str);
            if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(VC)) {
                JsonObject vc = jsonElement.getAsJsonObject().getAsJsonObject("VC");
                vaccinationEvents.addAll(smartHealthCardService.parseBundleVaccinationsFromVC(vc));
            } else {
                // TODO figure bundle version and parse
            }
        }
        return ResponseEntity.ok(vaccinationEvents);
    }

    private List<String> readHealthLink(String url, Optional<String> password, Optional<String> jwk) {
        if (!url.contains(SHLINK_PREFIX)) {
            throw new RuntimeException("Invalid shlink");
        }
        String shlink = SHLINK_PREFIX + url.split(SHLINK_PREFIX)[1];
        PublicKey publicKey = null;
        if (StringUtils.isNotBlank(jwk.orElse(null))) {
            publicKey = (PublicKey) Jwks.parser().build().parse(jwk.get()).toKey();
//            try {
////                publicKey = ECKey.parse(jwk.get()).toPublicKey();
//            } catch (JOSEException | ParseException e) {
//                throw new RuntimeException(e);
//            }
        }
        List<String> body = smartHealthLinksService.importSmartHealthLink(shlink, password.orElse(null), publicKey);
        return body;
    }


}
