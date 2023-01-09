package org.immregistries.ehr.api;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class IdentifierRegistryConfig {

    @Bean("ImmunizationIdentifier")
    public Map<Integer,Map<String,Integer>> immunizationIdentifierRegistry() {
        /**
         * key : ImmRegistry id
         *
         * value :
         *  key : external identifier
         *  value : internal id
         */
        return new HashMap(10);
    }

    @Bean("PatientIdentifier")
    public Map<Integer,Map<String,Integer>> patientIdentifierRegistry() {
        /**
         * key : ImmRegistry id
         *
         * value :
         *  key : external identifier
         *  value : internal id
         */
        return new HashMap(10);
    }

}
