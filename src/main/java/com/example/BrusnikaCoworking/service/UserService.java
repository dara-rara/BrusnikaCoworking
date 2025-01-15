package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.UserRepository;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.profile.UserBlock;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.LogupUser;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.EditPassword;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.ScanUser;
import com.example.BrusnikaCoworking.domain.reserval.State;
import com.example.BrusnikaCoworking.domain.user.Role;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import com.example.BrusnikaCoworking.exception.EmailException;
import com.example.BrusnikaCoworking.exception.InternalServerErrorException;
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

    public List<UserBlock> getBlockUsers(String prefix, Long id) {
        var entities = userRepository.findByEmailStartingWith(prefix.toLowerCase(), id);
        List<UserBlock> users = new ArrayList<>();
        for(var item : entities) {
            var state = State.TRUE;
            if(item.getCountBlock() > 2) state=State.FALSE;
            users.add(new UserBlock(
                    item.getId_user(),
                    item.getUsername(),
                    item.getRealname(),
                    item.getCountBlock(),
                    state
                    ));
        }
        return users;
    }

//    public List<UserBlock> getListBlockUser() {
//        var users = userRepository.findByCountBlockGreaterThan(2);
//        List<UserBlock> list = new ArrayList<>();
//        for (var item : users) {
//           var form = new UserBlock(
//                   item.getId_user(),
//                   item.getUsername(),
//                   item.getRealname(),
//                   item.getCountBlock()
//           );
//           list.add(form);
//        }
//        return list;
//    }

    public MessageResponse createBlock(Long id) {
        var user = getUserId(id);
        user.setCountBlock(3);
        userRepository.save(user);
        return new MessageResponse("user blocked");
    }

    public MessageResponse createUnblock(Long id) {
        var user = getUserId(id);
        user.setCountBlock(0);
        userRepository.save(user);
        return new MessageResponse("user unblocked");
    }

    public UserEntity getUserId(Long id) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) throw new ResourceException("user not found");
        return user.get();
     }

    public UserEntity createUser(LogupUser registrationRequest) {
        if (usernameExists(registrationRequest.getUsername())) {
            throw new EmailException("email: %s registered yet".formatted(registrationRequest.getUsername()));
        }
        var newUser = new UserEntity();
        newUser.setUsername(registrationRequest.getUsername());
        newUser.setRealname(registrationRequest.getRealname());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setCountBlock(0);
        newUser.setRole(Role.USER);
        try {
            return userRepository.save(newUser);
        } catch (Exception e) {
            throw new InternalServerErrorException("error datasource");
        }
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

    public List<ScanUser> getUsers(String prefix, Long id) {
        var entities = userRepository.findByEmailStartingWith(prefix.toLowerCase(), id);
        List<ScanUser> users = new ArrayList<>();
        for(var item : entities) {
            users.add(new ScanUser(item.getId_user(), item.getUsername(), item.getRealname()));
        }
        return users;
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
