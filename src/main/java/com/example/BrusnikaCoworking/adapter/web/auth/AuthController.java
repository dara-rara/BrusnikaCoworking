package com.example.BrusnikaCoworking.adapter.web.auth;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.*;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import com.example.BrusnikaCoworking.exception.EmailRegisteredException;
import com.example.BrusnikaCoworking.exception.LinkExpiredException;
import com.example.BrusnikaCoworking.service.AuthService;
import com.example.BrusnikaCoworking.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping(value = "/Brusnika/", produces = APPLICATION_JSON_VALUE)
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<?> signUp(@RequestBody LogupUser request) {
        try {
            if (userService.validEmail(request.getUsername())) {
                return ResponseEntity.ok(authService.signUp(request));
            }
            throw new Exception();
        } catch (Exception e) {
            throw new EmailRegisteredException("Email error");
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody MessageResponse data) {
        try {
            return ResponseEntity.ok(authService.confirmRegistration(data.message()));
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

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal UserEntity user) {
        try{
            return ResponseEntity.ok(new StatusResponse(user.getRole()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponse("the access token is not valid"));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("test");
    }
}
