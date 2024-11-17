package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.JwtAuthenticationResponse;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.LoginUser;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.LogupUser;
import com.example.BrusnikaCoworking.config.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Value("${token.signing.keyAccess}")
    private String jwtSigningKeyAccess;
    @Value("${token.signing.timeAccess}")
    private Long jwtSigningTimeAccess;

    @Value("${token.signing.keyRefresh}")
    private String jwtSigningKeyRefresh;
    @Value("${token.signing.timeRefresh}")
    private Long jwtSigningTimeRefresh;


    //Регистрация пользователя
    public JwtAuthenticationResponse signUp(LogupUser request) throws Exception {
        var user = userService.createUser(request);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
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
