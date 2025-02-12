package com.example.BrusnikaCoworking.adapter.web.admin;

import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.Date;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.ReservalAdminForm;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditPassword;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditRealname;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.DateAndTime;
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
@RequestMapping(value = "/Brusnika/admin/", produces = APPLICATION_JSON_VALUE)
public class AdminController {
    private final ReservalService reservalService;
    private final UserService userService;
    private final ProfileNotificationService profileNotificationService;

//    @PostMapping("/cancel/{id}")
//    public ResponseEntity<?> cancelReserval(@PathVariable Long id) {
//        return ResponseEntity.ok(reservalService.cancelReservalAdmin(id));
//    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/reservals")
    public ResponseEntity<?> getActiveReserval(@RequestBody Date date) {
        return ResponseEntity.ok(reservalService.reservalsActiveUserDate(date));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/list/{prefix}")
    public ResponseEntity<?> getUserGroupReserval(@PathVariable String prefix,
                                                  @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(userService.getBlockUsers(prefix, user.getId_user()));
    }

//    @GetMapping("/listBlock")
//    public ResponseEntity<?> getUserBlock() {
//        return ResponseEntity.ok(userService.getListBlockUser());
//    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/block/{id}")
    public ResponseEntity<?> block(@PathVariable Long id) {
        return ResponseEntity.ok(userService.createBlock(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/unblock/{id}")
    public ResponseEntity<?> unblock(@PathVariable Long id) {

        return ResponseEntity.ok(userService.createUnblock(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(profileNotificationService.getProfileAdmin(user));
    }

//    @PostMapping("/updatePassword")
//    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserEntity user,
//                                            @RequestBody EditPassword editPassword) {
//        return ResponseEntity.ok(userService.updatePasswordProfile(user, editPassword));
//    }
//
//    @PostMapping("/updateRealname")
//    public ResponseEntity<?> updateRealname(@AuthenticationPrincipal UserEntity user,
//                                            @RequestBody EditRealname editRealname) {
//        userService.updateRealname(user, editRealname.realname());
//        return ResponseEntity.ok(profileNotificationService.getProfile(user));
//    }
//
//    @PostMapping("/busyTables")
//    public ResponseEntity<?> getFreeTables(@RequestBody DateAndTime dateAndTime) {
//        return ResponseEntity.ok(reservalService.getBusyTables(dateAndTime));
//    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/reserval")
    public ResponseEntity<?> createReserval(@RequestBody ReservalAdminForm form,
                                            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok().body(reservalService.createAdminReserval(form, user));
    }

}
