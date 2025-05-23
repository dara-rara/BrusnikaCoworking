package com.example.BrusnikaCoworking.adapter.web.user;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.StatusResponse;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditPassword;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditRealname;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.Code;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.DateAndTime;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.ReservalForm;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import com.example.BrusnikaCoworking.service.ProfileNotificationService;
import com.example.BrusnikaCoworking.service.ReservalService;
import com.example.BrusnikaCoworking.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping(value = "/Brusnika/user/", produces = APPLICATION_JSON_VALUE)
public class UserController {
    private final ReservalService reservalService;
    private final ProfileNotificationService profileNotificationService;
    private final UserService userService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(profileNotificationService.getProfile(user));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserEntity user,
                                            @RequestBody EditPassword editPassword) {
        return ResponseEntity.ok(userService.updatePasswordProfile(user, editPassword));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @PostMapping("/updateRealname")
    public ResponseEntity<?> updateRealname(@AuthenticationPrincipal UserEntity user,
                                            @RequestBody EditRealname editRealname) {
        userService.updateRealname(user, editRealname.realname());
        return ResponseEntity.ok(profileNotificationService.getProfile(user));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/groupReserval/{id}")
    public ResponseEntity<?> confirmGroupReserval(@PathVariable Long id) {
        return ResponseEntity.ok(profileNotificationService.confirmGroupReserval(id));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/codeReserval/{id}")
    public ResponseEntity<?> confirmCodeReserval(@PathVariable Long id, @RequestBody Code response) {
        return ResponseEntity.ok(profileNotificationService.confirmReservalCode(id, response));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @GetMapping("/cancel/{id}")
    public ResponseEntity<?> cancelReserval(@PathVariable Long id, @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(reservalService.cancelReserval(id, user));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/group/{prefix}")
    public ResponseEntity<?> getUserGroupReserval(@PathVariable String prefix,
                                                  @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(userService.getUsers(prefix, user.getId_user()));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/notifications")
    public ResponseEntity<?> getAllNotification(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(profileNotificationService.getListsNotificationAndReserval(user));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @PostMapping("/busyTables")
    public ResponseEntity<?> getFreeTables(@RequestBody DateAndTime dateAndTime) {
        return ResponseEntity.ok(reservalService.getBusyTables(dateAndTime));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/reserval")
    public ResponseEntity<?> createReserval(@RequestBody ReservalForm form,
                                            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok().body(reservalService.createReserval(form, user));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(new StatusResponse(user.getRole()));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("test");
    }

}
