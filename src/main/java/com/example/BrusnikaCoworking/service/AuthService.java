package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.*;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.NewPassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.UpdatePassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.token.JwtAuthenticationResponse;
import com.example.BrusnikaCoworking.config.jwt.JwtService;
import com.example.BrusnikaCoworking.config.kafka.KafkaProducer;
import com.example.BrusnikaCoworking.exception.EmailException;
import com.example.BrusnikaCoworking.exception.LinkExpiredException;
import com.example.BrusnikaCoworking.exception.PasswordException;
import com.example.BrusnikaCoworking.exception.ResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CodingService codingService;
    private final KafkaProducer kafkaProducer;

    @Value("${token.signing.keyAccess}")
    private String jwtSigningKeyAccess;
    @Value("${token.signing.timeAccess}")
    private Long jwtSigningTimeAccess;

    @Value("${token.signing.keyRefresh}")
    private String jwtSigningKeyRefresh;
    @Value("${token.signing.timeRefresh}")
    private Long jwtSigningTimeRefresh;

    private static final String EMAIL_TOPIC_REG = "email_message_registration";
    private static final String EMAIL_TOPIC_PAS = "email_message_password";

    //Восстановление пароля
    public MessageResponse updatePassword(UpdatePassword updatePassword) {
        updatePassword.setUsername(updatePassword.getUsername().toLowerCase());
        if (!userService.usernameExists(updatePassword.getUsername())) {
            throw new EmailException("email: %s not found".formatted(updatePassword.getUsername()));
        }
        var dataToSend = codingService.encode(updatePassword);
        kafkaProducer.produce(EMAIL_TOPIC_PAS, new KafkaMailMessage(updatePassword.getUsername(), dataToSend));
        return new MessageResponse("password updated");
    }

    //Подтверждение изменения пароля через почту
    public JwtAuthenticationResponse confirmPassword(NewPassword newPassword) {
        var passwordDTO = codingService.decode(newPassword.data(), UpdatePassword.class);

        if (passwordDTO.getTime().isBefore(LocalDateTime.now().minusDays(1))) {
            throw new LinkExpiredException("the link has expired");
        }
        if (!userService.usernameExists(passwordDTO.getUsername())) {
            throw new EmailException("email: %s not found".formatted(passwordDTO.getUsername()));
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
        request.setUsername(request.getUsername().toLowerCase());
        if (!userService.validEmail(request.getUsername())) {
            throw new EmailException("email: %s does not belong to urfu".formatted(request.getUsername()));
        }
        if (userService.usernameExists(request.getUsername())) {
            throw new EmailException("email: %s registered yet".formatted(request.getUsername()));
        }
        var dataToSend = codingService.encode(request);
        kafkaProducer.produce(EMAIL_TOPIC_REG, new KafkaMailMessage(request.getUsername(), dataToSend));
        return new MessageResponse("link sent");
    }

    //Подтверждение регистрации через почту
    public JwtAuthenticationResponse confirmRegistration(String hash) {
        var userDTO = codingService.decode(hash, LogupUser.class);

        if (userDTO.getTime().isBefore(LocalDateTime.now().minusDays(1))) {
            throw new LinkExpiredException("the link has expired");
        }
        if (userService.usernameExists(userDTO.getUsername())) {
            throw new EmailException("email: %s registered yet".formatted(userDTO.getUsername()));
        }

        var user = userService.createUser(userDTO);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userDTO.getUsername(),
                    userDTO.getPassword()
            ));
        } catch (Exception e) {
            throw new PasswordException("the password you selected is incorrect");
        }
        var jwtAccess = jwtService.generateToken(user, jwtSigningKeyAccess, jwtSigningTimeAccess);
        var jwtRefresh = jwtService.generateToken(user, jwtSigningKeyRefresh, jwtSigningTimeRefresh);
        var encryptToken = tokenService.editToken(jwtRefresh);
        return new JwtAuthenticationResponse(jwtAccess, encryptToken, user.getRole());
    }

    //Аутентификация пользователя
    public JwtAuthenticationResponse signIn(LoginUser request) {
        request.setUsername(request.getUsername().toLowerCase());
        if (!userService.usernameExists(request.getUsername())) {
            throw new EmailException("email: %s not registered".formatted(request.getUsername()));
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            ));
        } catch (Exception e) {
            if (!passwordEncoder.matches(request.getPassword(),
                    userService.getByUsername(request.getUsername()).getPassword()))
                throw new PasswordException("the password you selected is incorrect");
            throw new ResourceException(e.getMessage());
        }

        var user = userService.loadUserByUsername(request.getUsername());
        var jwtAccess = jwtService.generateToken(user, jwtSigningKeyAccess, jwtSigningTimeAccess);
        var jwtRefresh = jwtService.generateToken(user, jwtSigningKeyRefresh, jwtSigningTimeRefresh);
        var encryptToken = tokenService.editToken(jwtRefresh);
        return new JwtAuthenticationResponse(jwtAccess, encryptToken, user.getRole());
    }

    //Генерация access token при валидном refresh token
    public JwtAuthenticationResponse getNewAccessToken(String encryptToken) {
        var token = tokenService.getToken(encryptToken);
        var username = jwtService.extractUserName(token, jwtSigningKeyRefresh);
        var user = userService.loadUserByUsername(username);
        var jwtAccess = jwtService.generateToken(user, jwtSigningKeyAccess, jwtSigningTimeAccess);
        return new JwtAuthenticationResponse(jwtAccess, encryptToken, user.getRole());
    }
}
