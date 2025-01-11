package com.example.BrusnikaCoworking.adapter.web.user;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.StatusResponse;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditPassword;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditRealname;
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
@PreAuthorize("hasAuthority('USER')")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping(value = "/Brusnika/user/", produces = APPLICATION_JSON_VALUE)
public class UserController {
    private final ReservalService reservalService;
    private final ProfileNotificationService profileNotificationService;
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(profileNotificationService.getProfile(user));
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserEntity user,
                                            @RequestBody EditPassword editPassword) {
        return ResponseEntity.ok(userService.updatePasswordProfile(user, editPassword));
    }

    @PostMapping("/updateRealname")
    public ResponseEntity<?> updateRealname(@AuthenticationPrincipal UserEntity user,
                                            @RequestBody EditRealname editRealname) {
        userService.updateRealname(user, editRealname.realname());
        return ResponseEntity.ok(profileNotificationService.getProfile(user));
    }

    @GetMapping("/groupReserval/{id}")
    public ResponseEntity<?> confirmGroupReserval(@PathVariable Long id) {
        return ResponseEntity.ok(profileNotificationService.confirmGroupReserval(id));
    }

    @GetMapping("/cancel/{id}")
    public ResponseEntity<?> cancelReserval(@PathVariable Long id, @AuthenticationPrincipal UserEntity user) {
        reservalService.cancelReserval(id);
        return ResponseEntity.ok(profileNotificationService.getListsNotificationAndReserval(user));
    }

    @GetMapping("/group/{username}")
    public ResponseEntity<?> getUserGroupReserval(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUser(username));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getAllNotification(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(profileNotificationService.getListsNotificationAndReserval(user));
    }

    @PostMapping("/busyTables")
    public ResponseEntity<?> getFreeTables(@RequestBody DateAndTime dateAndTime) {
        return ResponseEntity.ok(reservalService.getBusyTables(dateAndTime));
    }

    @PostMapping("/reserval")
    public ResponseEntity<?> createReserval(@RequestBody ReservalForm form,
                                            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok().body(reservalService.createReserval(form, user));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(new StatusResponse(user.getRole()));
    }

//    @GetMapping("/test")
//    public ResponseEntity<?> test() {
//        return ResponseEntity.ok("test");
//    }

}
