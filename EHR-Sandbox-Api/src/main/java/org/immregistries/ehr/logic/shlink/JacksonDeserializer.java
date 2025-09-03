package org.immregistries.ehr.logic.shlink;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.io.Deserializer;

import java.io.IOException;
import java.io.Reader;

/**
 * A Deserializer implementation for JSON Web Tokens (JWT) using Jackson.
 * This class is designed to be injected into a JWT builder to handle the
 * deserialization of the JWT claims.
 */
public class JacksonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a JacksonDeserializer with a default ObjectMapper.
     */
    public JacksonDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a JacksonDeserializer with a custom ObjectMapper.
     *
     * @param objectMapper The ObjectMapper to use for deserialization.
     */
    public JacksonDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Deserializes a byte array into an object of type T.
     *
     * @param bytes The byte array to deserialize.
     * @return The deserialized object.
     * @throws DeserializationException if the deserialization fails.
     */
    @Override
    public T deserialize(byte[] bytes) throws DeserializationException {
        try {
            return (T) objectMapper.readValue(bytes, Object.class);
        } catch (IOException e) {
            throw new DeserializationException("Failed to deserialize bytes into an object.", e);
        }
    }

    @Override
    public T deserialize(Reader reader) throws DeserializationException {
        try {
            return (T) objectMapper.readValue(reader, Object.class);
        } catch (IOException e) {
            throw new DeserializationException("Failed to deserialize bytes into an object.", e);
        }
    }
}
