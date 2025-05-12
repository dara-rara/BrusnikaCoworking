package com.example.BrusnikaCoworking.config;

import com.example.BrusnikaCoworking.adapter.repository.CoworkingRepository;
import com.example.BrusnikaCoworking.adapter.repository.UserRepository;
import com.example.BrusnikaCoworking.domain.reserval.CoworkingEntity;
import com.example.BrusnikaCoworking.domain.user.Role;
import com.example.BrusnikaCoworking.service.CodingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminCommandLineRunner implements CommandLineRunner {
    private final CoworkingRepository coworkingRepository;
    private final CodingService codingService;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        var user = userRepository.findByUsername("Diana.Sufianova@urfu.me");
        if (user.isPresent()) {
            var ent = user.get();
            ent.setRole(Role.ADMIN);
            userRepository.save(ent);
        }
//        if (userRepository.findByUsername("user@urfu.me").isEmpty()) {
//            var user = new UserEntity();
//            user.setUsername("user@urfu.me");
//            user.setRealname("user");
//            user.setPassword(passwordEncoder.encode("111111"));
//            user.setRole(Role.USER);
//            user.setCountBlock(0);
//            userRepository.save(user);
//        }
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
