package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.exception.CodingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CodingService {
    private final ObjectMapper objectMapper;


    public CodingService(
            @Qualifier("customObjectMapper") ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public <T> T decode(String data, Class<T> to) {
        try {

            byte[] decodedBytes = Base64.getDecoder().decode(data);

            String jsonData = new String(decodedBytes);

            return objectMapper.readValue(jsonData,to);

        } catch (Exception e) {
            throw new CodingException("failed to decode or convert the data");
        }
    }

    public <T> String encode(T t) {
        String jsonData = null;
        try {
            jsonData = objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new CodingException(e.getMessage());
        }
        return Base64.getEncoder().encodeToString(jsonData.getBytes());
    }
}
