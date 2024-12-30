package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.UserRepository;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.LogupUser;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditPassword;
import com.example.BrusnikaCoworking.domain.user.Role;
import com.example.BrusnikaCoworking.domain.user.StatusBlock;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService implements UserDetailsService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public UserEntity createUser(LogupUser registrationRequest) {
        if (usernameExists(registrationRequest.getUsername())) {
            throw new UsernameNotFoundException(registrationRequest.getUsername());
        }
        var newUser = new UserEntity();
        newUser.setUsername(registrationRequest.getUsername());
        newUser.setRealname(registrationRequest.getRealname());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setStatusBlock(StatusBlock.UNBLOCK);
        newUser.setRole(Role.USER);
        return userRepository.save(newUser);
    }

    public UserEntity updatePasswordEmail(UserEntity user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public MessageResponse updatePasswordProfile(UserEntity user, EditPassword editPassword) {
        if (passwordEncoder.matches(editPassword.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(editPassword.newPassword()));
            userRepository.save(user);
            return new MessageResponse("Update password");
        }
        return new MessageResponse("The password is incorrect");
    }

    public void updateRealname(UserEntity user, String realname) {
        user.setRealname(realname);
        userRepository.save(user);
    }

    public boolean usernameExists(String name) {
        return userRepository.existsByUsername(name);
    }

    public UserEntity getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean validEmail(String username) {
        var domen = username.split("@");
        return !usernameExists(username.toLowerCase())
                && (domen[1].equals("urfu.me") || domen[1].equals("at.urfu.ru"));
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return getByUsername(username);
    }
}
