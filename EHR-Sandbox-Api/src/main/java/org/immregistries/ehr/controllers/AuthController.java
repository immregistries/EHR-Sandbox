package org.immregistries.ehr.controllers;

import javax.validation.Valid;

import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.entities.User;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.repositories.UserRepository;
import org.immregistries.ehr.security.JwtResponse;
import org.immregistries.ehr.security.JwtUtils;
import org.immregistries.ehr.security.UserDetailsImpl;
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
        }
        // Create new user's account
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(encoder.encode(user.getPassword()));
        ImmunizationRegistry immunizationRegistry = new ImmunizationRegistry();
        immunizationRegistry.setName("Localhost");
        immunizationRegistry.setIisFacilityId(newUser.getUsername());
        immunizationRegistry.setIisUsername(newUser.getUsername());
        immunizationRegistry.setIisPassword(newUser.getUsername());
        immunizationRegistry.setIisHl7Url("http://localhost:8080/iis-sandbox-jpa/soap");
        immunizationRegistry.setIisFhirUrl("http://localhost:8080/iis-sandbox-jpa/fhir");
        immunizationRegistry.setUser(newUser);
        userRepository.save(newUser);
        immunizationRegistryRepository.save(immunizationRegistry);
        return registerUser(user);
    }
}