package com.example.BrusnikaCoworking.adapter.web.auth;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.*;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.NewPassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.UpdatePassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.token.UpdateToken;
import com.example.BrusnikaCoworking.exception.EmailRegisteredException;
import com.example.BrusnikaCoworking.exception.LinkExpiredException;
import com.example.BrusnikaCoworking.service.AuthService;
import com.example.BrusnikaCoworking.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping(value = "/Brusnika/auth/", produces = APPLICATION_JSON_VALUE)
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<?> signUp(@RequestBody LogupUser request) {
        try {
            if (userService.validEmail(request.getUsername())) {
                return ResponseEntity.ok(authService.signUp(request));
            }
            throw new EmailRegisteredException("Email error");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error"));
        }
    }

    @PostMapping("/confirmReg")
    public ResponseEntity<?> confirmRegistration(@RequestBody MessageResponse data) {
        try {
            return ResponseEntity.ok(authService.confirmRegistration(data.message()));
        } catch (Exception e) {
            throw new LinkExpiredException("The link is not valid");
        }
    }

    @PostMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePassword updatePassword) {
        try {
            if (userService.usernameExists(updatePassword.getUsername())) {
                return ResponseEntity.ok(authService.updatePassword(updatePassword));
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Email not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error"));
        }
    }

    @PostMapping("/confirmPas")
    public ResponseEntity<?> confirmPassword(@RequestBody NewPassword newPassword) {
        try {
            return ResponseEntity.ok(authService.confirmPassword(newPassword));
        } catch (Exception e) {
            throw new LinkExpiredException("The link is not valid");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody LoginUser request) {
        try {
            if (!userService.usernameExists(request.username())) {
                throw new EmailRegisteredException("Email: %s not registered".formatted(request.username()));
            }
            return ResponseEntity.ok(authService.signIn(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("The password you selected is incorrect"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody UpdateToken token) {
        try{
            var response = authService.getNewAccessToken(token.token());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new MessageResponse("the refresh token is not valid"));
        }
    }
}
