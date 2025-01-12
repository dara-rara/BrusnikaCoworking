package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.CodeRepository;
import com.example.BrusnikaCoworking.domain.reserval.CodeEntity;
import com.example.BrusnikaCoworking.exception.CodingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Random;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CodingService {
    private final ObjectMapper objectMapper;
    public final CodeRepository codeRepository;
    private static final String allowedChars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_-+=[]{}|;:',.<>/?`~";


    public CodingService(
            @Qualifier("customObjectMapper") ObjectMapper objectMapper,
            CodeRepository codeRepository) {
        this.objectMapper = objectMapper;
        this.codeRepository = codeRepository;
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

    public void getCodeForReserval() {
        var now = LocalDateTime.now();

        var random = new Random();
        var sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            // Генерация случайного индекса в пределах длины строки allowedChars
            int randomIndex = random.nextInt(allowedChars.length());
            // Добавляем случайный символ из allowedChars
            sb.append(allowedChars.charAt(randomIndex));
        }

        var code = new CodeEntity();
        code.setSendTime(now);
        code.setCode(sb.toString());
        codeRepository.save(code);
    }
}
