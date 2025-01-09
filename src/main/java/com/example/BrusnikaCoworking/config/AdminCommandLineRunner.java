package com.example.BrusnikaCoworking.config;

import com.example.BrusnikaCoworking.adapter.repository.CoworkingRepository;
import com.example.BrusnikaCoworking.adapter.repository.UserRepository;
import com.example.BrusnikaCoworking.domain.reserval.CoworkingEntity;
import com.example.BrusnikaCoworking.domain.user.Role;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminCommandLineRunner implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CoworkingRepository coworkingRepository;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            var user = new UserEntity();
            user.setUsername("admin");
            user.setRealname("admin");
            user.setPassword(passwordEncoder.encode("admin"));
            user.setRole(Role.ADMIN);
            user.setCountBlock(0);
//            user.setStatusBlock(StatusBlock.UNBLOCK);
            userRepository.save(user);
        }
        if (userRepository.findByUsername("user").isEmpty()) {
            var user = new UserEntity();
            user.setUsername("user");
            user.setRealname("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setRole(Role.USER);
            user.setCountBlock(0);
//            user.setStatusBlock(StatusBlock.BLOCK);
            userRepository.save(user);
        }
        if (coworkingRepository.count() == 0) {
            for (var i = 0; i < 24; i++) {
                var table = new CoworkingEntity();
                table.setNumber(i + 1);
                coworkingRepository.save(table);
            }
        }
    }
}
