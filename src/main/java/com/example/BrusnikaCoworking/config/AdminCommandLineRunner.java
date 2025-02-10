package com.example.BrusnikaCoworking.config;

import com.example.BrusnikaCoworking.adapter.repository.CoworkingRepository;
import com.example.BrusnikaCoworking.adapter.repository.UserRepository;
import com.example.BrusnikaCoworking.domain.reserval.CoworkingEntity;
import com.example.BrusnikaCoworking.domain.user.Role;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import com.example.BrusnikaCoworking.service.CodingService;
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
    private final CodingService codingService;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin@urfu.me").isEmpty()) {
            var user = new UserEntity();
            user.setUsername("admin@urfu.me");
            user.setRealname("admin");
            user.setPassword(passwordEncoder.encode("123123"));
            user.setRole(Role.ADMIN);
            user.setCountBlock(0);
//            user.setStatusBlock(StatusBlock.UNBLOCK);
            userRepository.save(user);
        }
        if (userRepository.findByUsername("user@urfu.me").isEmpty()) {
            var user = new UserEntity();
            user.setUsername("user@urfu.me");
            user.setRealname("user");
            user.setPassword(passwordEncoder.encode("111111"));
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
        codingService.getCodeForReserval();
    }
}
