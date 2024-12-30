package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.*;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.NewPassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.UpdatePassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.token.JwtAuthenticationResponse;
import com.example.BrusnikaCoworking.config.jwt.JwtService;
import com.example.BrusnikaCoworking.config.kafka.KafkaProducer;
import com.example.BrusnikaCoworking.exception.EmailRegisteredException;
import com.example.BrusnikaCoworking.exception.LinkExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final Base64Service base64Service;
    private final KafkaProducer kafkaProducer;

    @Value("${token.signing.keyAccess}")
    private String jwtSigningKeyAccess;
    @Value("${token.signing.timeAccess}")
    private Long jwtSigningTimeAccess;

    @Value("${token.signing.keyRefresh}")
    private String jwtSigningKeyRefresh;
    @Value("${token.signing.timeRefresh}")
    private Long jwtSigningTimeRefresh;

    public static final String EMAIL_TOPIC_REG = "email_message_registration";
    public static final String EMAIL_TOPIC_PAS = "email_message_password";

    //Обновление пароля
    public MessageResponse updatePassword(UpdatePassword updatePassword) {
        try {
            if (!userService.usernameExists(updatePassword.getUsername())) {
                throw new EmailRegisteredException("email: %s not found".formatted(updatePassword.getUsername()));
            }

            var dataToSend = base64Service.encode(updatePassword);
            kafkaProducer.produce(EMAIL_TOPIC_PAS, new KafkaMailMessage(updatePassword.getUsername(), dataToSend));
            return new MessageResponse("ok");
        } catch (Exception e) {
            return new MessageResponse(e.getMessage());
        }
    }

    //Подтверждение изменения пароля через почту
    public JwtAuthenticationResponse confirmPassword(NewPassword newPassword) throws Exception{
        var passwordDTO = base64Service.decode(newPassword.data(), UpdatePassword.class);

        if (passwordDTO.getTime().isBefore(LocalDateTime.now().minusDays(1))) {
            throw new LinkExpiredException("The link has expired");
        }

        if (!userService.usernameExists(passwordDTO.getUsername())) {
            throw new EmailRegisteredException("email: %s not found".formatted(passwordDTO.getUsername()));
        }

        var user = userService.updatePasswordEmail(
                userService.getByUsername(passwordDTO.getUsername()),
                newPassword.password());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                newPassword.password()
        ));
        var jwtAccess = jwtService.generateToken(user, jwtSigningKeyAccess, jwtSigningTimeAccess);
        var jwtRefresh = jwtService.generateToken(user, jwtSigningKeyRefresh, jwtSigningTimeRefresh);
        var encryptToken = tokenService.editToken(jwtRefresh);
        return new JwtAuthenticationResponse(jwtAccess, encryptToken, user.getRole());
    }

    //Регистрация пользователя
    public MessageResponse signUp(LogupUser request) {
        try {
            if (userService.usernameExists(request.getUsername())) {
                throw new EmailRegisteredException("email: %s registered yet".formatted(request.getUsername()));
            }

            var dataToSend = base64Service.encode(request);
            kafkaProducer.produce(EMAIL_TOPIC_REG, new KafkaMailMessage(request.getUsername(), dataToSend));
            return new MessageResponse("ok");
        } catch (Exception e) {
            return new MessageResponse(e.getMessage());
        }
    }

    //Подтверждение регистрации через почту
    public JwtAuthenticationResponse confirmRegistration(String hash) throws Exception {
        var userDTO = base64Service.decode(hash, LogupUser.class);

        if (userDTO.getTime().isBefore(LocalDateTime.now().minusDays(1))) {
            throw new LinkExpiredException("The link has expired");
        }
        var user = userService.createUser(userDTO);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                userDTO.getPassword()
        ));
        var jwtAccess = jwtService.generateToken(user, jwtSigningKeyAccess, jwtSigningTimeAccess);
        var jwtRefresh = jwtService.generateToken(user, jwtSigningKeyRefresh, jwtSigningTimeRefresh);
        var encryptToken = tokenService.editToken(jwtRefresh);
        return new JwtAuthenticationResponse(jwtAccess, encryptToken, user.getRole());
    }

    //Аутентификация пользователя
    public JwtAuthenticationResponse signIn(LoginUser request) throws Exception {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));
        var user = userService.loadUserByUsername(request.username());
        var jwtAccess = jwtService.generateToken(user, jwtSigningKeyAccess, jwtSigningTimeAccess);
        var jwtRefresh = jwtService.generateToken(user, jwtSigningKeyRefresh, jwtSigningTimeRefresh);
        var encryptToken = tokenService.editToken(jwtRefresh);
        return new JwtAuthenticationResponse(jwtAccess, encryptToken, user.getRole());
    }

    //Генерация access token при валидном refresh token
    public JwtAuthenticationResponse getNewAccessToken(String encryptToken) throws Exception {
        var token = tokenService.getToken(encryptToken);
        var username = jwtService.extractUserName(token, jwtSigningKeyRefresh);
        var user = userService.loadUserByUsername(username);
        var jwtAccess = jwtService.generateToken(user, jwtSigningKeyAccess, jwtSigningTimeAccess);
        return new JwtAuthenticationResponse(jwtAccess, encryptToken, user.getRole());
    }
}
