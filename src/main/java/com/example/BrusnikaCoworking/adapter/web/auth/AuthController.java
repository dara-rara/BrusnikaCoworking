package com.example.BrusnikaCoworking.adapter.web.auth;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.*;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
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
    public ResponseEntity<?> signUp(@RequestBody LogupUser request) throws Exception {
        if (userService.usernameExists(request.username())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Такой email уже существует!"));
        }
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody LoginUser request) {
        try {
            if (!userService.usernameExists(request.username())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Такой email не существует!"));
            }
            return ResponseEntity.ok(authService.signIn(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Неправильно набран пароль!"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody UpdateToken token) {
        try{
            var response = authService.getNewAccessToken(token.token());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new MessageResponse("Refresh token не валиден!"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(new StatusResponse(user.getRole()));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("test");
    }
}
