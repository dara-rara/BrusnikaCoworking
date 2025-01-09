package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.UserRepository;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.profile.UserBlock;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.LogupUser;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditPassword;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.ScanUser;
import com.example.BrusnikaCoworking.domain.user.Role;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import com.example.BrusnikaCoworking.exception.PasswordException;
import com.example.BrusnikaCoworking.exception.ResourceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService implements UserDetailsService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ScanUser getUser(String username) {
        var user = getByUsername(username);
        return new ScanUser(user.getId_user(), user.getUsername(), user.getRealname());
    }

    public UserBlock getBlockUser(String username) {
        var user = getByUsername(username);
        return new UserBlock(user.getId_user(), user.getUsername(), user.getRealname(), user.getCountBlock());
    }

    public List<UserBlock> getListBlockUser() {
        var users = userRepository.findByCountBlockGreaterThan(2);
        List<UserBlock> list = new ArrayList<>();
        for (var item : users) {
           var form = new UserBlock(
                   item.getId_user(),
                   item.getUsername(),
                   item.getRealname(),
                   item.getCountBlock()
           );
           list.add(form);
        }
        return list;
    }

    public void createBlock(Long id) {
        var user = getUserId(id);
        user.setCountBlock(3);
        userRepository.save(user);
    }

    public void createUnblock(Long id) {
        var user = getUserId(id);
        user.setCountBlock(0);
        userRepository.save(user);
    }

    public UserEntity getUserId(Long id) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) throw new  ResourceException("User not found");
        return user.get();
     }

    public UserEntity createUser(LogupUser registrationRequest) {
        if (usernameExists(registrationRequest.getUsername())) {
            throw new UsernameNotFoundException(registrationRequest.getUsername());
        }
        var newUser = new UserEntity();
        newUser.setUsername(registrationRequest.getUsername());
        newUser.setRealname(registrationRequest.getRealname());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
//        newUser.setStatusBlock(StatusBlock.UNBLOCK);
        newUser.setCountBlock(0);
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
            return new MessageResponse("update password");
        }
        throw new PasswordException("the password you selected is incorrect");
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
                .orElseThrow(() -> new ResourceException("user not found"));
    }

    public boolean validEmail(String username) {
        var domen = username.split("@");
        return domen[1].equals("urfu.me") || domen[1].equals("at.urfu.ru");
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return getByUsername(username);
    }
}
