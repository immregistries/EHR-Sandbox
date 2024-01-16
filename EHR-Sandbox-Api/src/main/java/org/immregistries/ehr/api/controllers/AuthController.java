package org.immregistries.ehr.api.controllers;

import javax.validation.Valid;

import org.immregistries.ehr.api.repositories.UserRepository;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.User;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.JwtResponse;
import org.immregistries.ehr.api.security.JwtUtils;
import org.immregistries.ehr.api.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
        if (user.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "no password specified");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername()));
        } else {
            createUser(user);
            return registerUser(user);
        }
    }

    private synchronized void createUser( User user) {
        /**
         * Checking the existence again since method is synchronised and might create duplicates with request spam
         */
        if (!userRepository.existsByUsername(user.getUsername())) {
            // Create new user's account
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(newUser);

            /**
             * Defining default IIS's automatically on first login
             */
            ImmunizationRegistry immunizationRegistryOnline = new ImmunizationRegistry();
            immunizationRegistryOnline.setName("Online (sabbia)");
            immunizationRegistryOnline.setIisFacilityId(newUser.getUsername());
            immunizationRegistryOnline.setIisUsername(newUser.getUsername());
            immunizationRegistryOnline.setIisPassword(newUser.getUsername());
            immunizationRegistryOnline.setIisHl7Url("https://sabbia.immregistries.org/iis/soap");
            immunizationRegistryOnline.setIisFhirUrl("https://sabbia.immregistries.org/iis/fhir");
            immunizationRegistryOnline.setUser(newUser);
            immunizationRegistryRepository.save(immunizationRegistryOnline);

            ImmunizationRegistry immunizationRegistry = new ImmunizationRegistry();
            immunizationRegistry.setName("Localhost");
            immunizationRegistry.setIisFacilityId(newUser.getUsername());
            immunizationRegistry.setIisUsername(newUser.getUsername());
            immunizationRegistry.setIisPassword(newUser.getUsername());
            immunizationRegistry.setIisHl7Url("http://localhost:8080/iis/soap");
            immunizationRegistry.setIisFhirUrl("http://localhost:8080/iis/fhir");
            immunizationRegistry.setUser(newUser);
            immunizationRegistryRepository.save(immunizationRegistry);

        }
    }
}