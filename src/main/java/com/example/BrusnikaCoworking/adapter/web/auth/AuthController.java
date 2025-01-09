package com.example.BrusnikaCoworking.adapter.web.auth;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.*;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.NewPassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.UpdatePassword;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.token.UpdateToken;
import com.example.BrusnikaCoworking.exception.EmailException;
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
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody LoginUser request) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/confirmReg")
    public ResponseEntity<?> confirmRegistration(@RequestBody MessageResponse data) {
        return ResponseEntity.ok(authService.confirmRegistration(data.message()));
    }

    @PostMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePassword updatePassword) {
        return ResponseEntity.ok(authService.updatePassword(updatePassword));
    }

    @PostMapping("/confirmPas")
    public ResponseEntity<?> confirmPassword(@RequestBody NewPassword newPassword) {
        return ResponseEntity.ok(authService.confirmPassword(newPassword));
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
