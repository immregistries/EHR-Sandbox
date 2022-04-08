package org.immregistries.ehr.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;

import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.entities.User;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.repositories.UserRepository;
import org.immregistries.ehr.security.JwtResponse;
import org.immregistries.ehr.security.JwtUtils;
import org.immregistries.ehr.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ImmunizationRegistryRepository immunizationRegistryRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;


    @PostMapping( consumes = {"application/xml","application/json"})
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername()));
        }
        // Create new user's account
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(encoder.encode(user.getPassword()));
        ImmunizationRegistry immunizationRegistry = new ImmunizationRegistry();
        immunizationRegistry.setIisFacilityId(newUser.getUsername());
        immunizationRegistry.setIisUsername(newUser.getUsername());
        immunizationRegistry.setIisPassword(newUser.getUsername());
        immunizationRegistry.setIisHl7Url("https://florence.immregistries.org/iis-sandbox/soap");
        immunizationRegistry.setIisFhirUrl("https://florence.immregistries.org/iis-sandbox/fhir");
        immunizationRegistry.setUser(newUser);
//        newUser.setImmunizationRegistries((Set<ImmunizationRegistry>) immunizationRegistry);
        userRepository.save(newUser);
        immunizationRegistryRepository.save(immunizationRegistry);
        return registerUser(user);
    }
}