package com.jwtapp.service;

import com.jwtapp.model.UserEntity;
import com.jwtapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            UserEntity userEntity1 = new UserEntity();
            userEntity1.setUsername("test_user1");
            userEntity1.setPassword(passwordEncoder.encode("test_password1"));
            userEntity1.setRoles("USER");
            userRepository.save(userEntity1);

            UserEntity userEntity2 = new UserEntity();
            userEntity2.setUsername("test_user2");
            userEntity2.setPassword(passwordEncoder.encode("test_password2"));
            userEntity2.setRoles("USER");
            userRepository.save(userEntity2);
        }
    }
}
