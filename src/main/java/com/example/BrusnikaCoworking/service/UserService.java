package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.UserRepository;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.LogupUser;
import com.example.BrusnikaCoworking.domain.user.Role;
import com.example.BrusnikaCoworking.domain.user.Status;
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
        if (usernameExists(registrationRequest.username())) {
            throw new UsernameNotFoundException(registrationRequest.username());
        }
        var newUser = new UserEntity();
        newUser.setUsername(registrationRequest.username());
        newUser.setRealname(registrationRequest.realname());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.password()));
        newUser.setStatus(Status.UNBLOCK);
        newUser.setRole(Role.USER);
        return userRepository.save(newUser);
    }

    public UserEntity getById(Long id) {
        return userRepository.getReferenceById(id);
    }

    public boolean usernameExists(String name) {
        return userRepository.existsByUsername(name);
    }

    public UserEntity getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return getByUsername(username);
    }
}
