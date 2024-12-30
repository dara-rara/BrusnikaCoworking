package com.example.BrusnikaCoworking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {
    @Value("${token.aes}")
    private String keyAES;

    public String editToken(String newToken) throws Exception {
        var decodedKey = Base64.getDecoder().decode(keyAES);
        var secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        var encryptToken = encrypt(newToken, secretKey);
        return encryptToken;
    }

    public String getToken(String tokenDecrypt) throws Exception {
        var decodedKey = Base64.getDecoder().decode(keyAES);
        var secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        var decryptToken = decrypt(tokenDecrypt, secretKey);;
        return decryptToken;
    }

    // Метод для шифрования строки
    public String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Метод для расшифровки строки
    public String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}
