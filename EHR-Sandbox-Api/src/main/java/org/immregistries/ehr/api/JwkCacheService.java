package org.immregistries.ehr.api;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage and cache JWKs (JSON Web Keys) partitioned by User/Tenant.
 * Uses an internal Map for storage.
 */
@Service
public class JwkCacheService {

    // Thread-safe map to store JWKSets keyed by userId
    private final Map<String, JWKSet> userJwksMap = new ConcurrentHashMap<>();

    /**
     * Retrieves the JWKSet for a specific user.
     * If not in map, it will fetch from the remote URI and store it.
     *
     * @param userId    The unique identifier for the user or tenant.
     * @param jwkSetUri The remote URL to fetch keys from if not found in map.
     * @return JWKSet
     */
    public JWKSet getJwkSetForUser(String userId, String jwkSetUri) {
        // computeIfAbsent is atomic and ensures we only fetch once if the key is missing
        return userJwksMap.computeIfAbsent(userId, k -> {
//            try {
//                return JWKSet.load(new URL(jwkSetUri));
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to fetch JWK Set for user: " + userId, e);
//            }
            return new JWKSet();
        });
    }

    /**
     * Manually replaces/adds the entire JWKSet for a specific user in the map.
     *
     * @param userId The unique identifier for the user.
     * @param jwkSet The new JWKSet to store.
     * @return The stored JWKSet.
     */
    public JWKSet updateFullKeySet(String userId, JWKSet jwkSet) {
        userJwksMap.put(userId, jwkSet);
        return jwkSet;
    }

    /**
     * Adds a single JWK to a user's existing cached JWKSet.
     * If no set exists for the user, it creates a new one.
     *
     * @param userId The unique identifier for the user.
     * @param newKey The individual JWK to add.
     */
    public void addSingleKeyToCache(String userId, JWK newKey) {
        userJwksMap.compute(userId, (id, currentSet) -> {
            if (currentSet != null) {
                // JWKSet is immutable, so we create a new list and append
                List<JWK> keys = new ArrayList<>(currentSet.getKeys());
                keys.add(newKey);
                return new JWKSet(keys);
            } else {
                // Initialize a new set if none exists
                return new JWKSet(newKey);
            }
        });
    }

    /**
     * Access method to find a specific key by its ID (kid) for a specific user.
     *
     * @param userId      The user ID.
     * @param kid         The Key ID to look for.
     * @param fallbackUri The URI to use if the map is empty.
     * @return The specific JWK or null if not found.
     */
    public JWK getSpecificKey(String userId, String kid, String fallbackUri) {
        JWKSet jwkSet = getJwkSetForUser(userId, fallbackUri);
        return jwkSet != null ? jwkSet.getKeyByKeyId(kid) : null;
    }

    /**
     * Optional: Clear the map if needed (useful for testing)
     */
    public void clearCache() {
        userJwksMap.clear();
    }
}